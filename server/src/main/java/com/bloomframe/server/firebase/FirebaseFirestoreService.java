package com.bloomframe.server.firebase;

import com.bloomframe.server.dto.DeviceRegistrationDto;
import com.bloomframe.server.dto.MedicineAnalysisDto;
import com.bloomframe.server.dto.ReminderDto;
import com.bloomframe.server.dto.UserProfileDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Firebase Firestore implementation ??Backend A maintains this class.
 */
public class FirebaseFirestoreService implements FirestoreService {

    private final Firestore firestore;

    public FirebaseFirestoreService(FirebaseAppHolder holder) {
        this.firestore = FirestoreClient.getFirestore(holder.app());
    }

    @Override
    public void saveProfile(String uid, UserProfileDto profile) {
        Map<String, Object> data = new HashMap<>();
        data.put("userType", profile.userType());
        data.put("name", profile.name());
        data.put("phone", profile.phone());
        data.put("birthDate", profile.birthDate());
        data.put("updatedAt", com.google.cloud.Timestamp.now());
        await(firestore.collection("users").document(uid).set(data, com.google.cloud.firestore.SetOptions.merge()));
    }

    @Override
    public Optional<UserProfileDto> getProfile(String uid) {
        DocumentSnapshot doc = await(firestore.collection("users").document(uid).get());
        if (!doc.exists()) {
            return Optional.empty();
        }
        return Optional.of(new UserProfileDto(
                doc.getString("userType"),
                doc.getString("name"),
                doc.getString("phone"),
                doc.getString("birthDate")
        ));
    }

    @Override
    public ReminderDto saveReminder(String uid, ReminderDto reminder) {
        String id = reminder.id() != null && !reminder.id().isBlank()
                ? reminder.id()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        DocumentReference ref = firestore.collection("users").document(uid)
                .collection("reminders").document(id);
        await(ref.set(toReminderMap(reminder.withId(id))));
        return reminder.withId(id);
    }

    @Override
    public List<ReminderDto> listReminders(String uid) {
        QuerySnapshot snapshot = await(
                firestore.collection("users").document(uid).collection("reminders").get());
        List<ReminderDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            result.add(fromReminderDoc(doc.getId(), doc.getData()));
        }
        return result;
    }

    @Override
    public ReminderDto updateReminder(String uid, String reminderId, ReminderDto reminder) {
        DocumentReference ref = firestore.collection("users").document(uid)
                .collection("reminders").document(reminderId);
        await(ref.set(toReminderMap(reminder.withId(reminderId)), com.google.cloud.firestore.SetOptions.merge()));
        return reminder.withId(reminderId);
    }

    @Override
    public void registerDevice(String uid, DeviceRegistrationDto device) {
        String tokenKey = device.fcmToken().substring(0, Math.min(16, device.fcmToken().length()));
        Map<String, Object> data = new HashMap<>();
        data.put("platform", device.platform());
        data.put("fcmToken", device.fcmToken());
        data.put("updatedAt", com.google.cloud.Timestamp.now());
        await(firestore.collection("users").document(uid)
                .collection("devices").document(tokenKey).set(data));
    }

    private Map<String, Object> toReminderMap(ReminderDto reminder) {
        Map<String, Object> data = new HashMap<>();
        data.put("category", reminder.category());
        data.put("times", reminder.times());
        data.put("imageKey", reminder.imageKey());
        data.put("photoStoragePath", reminder.photoStoragePath());
        data.put("confirmed", reminder.confirmed());
        data.put("updatedAt", com.google.cloud.Timestamp.now());
        if (reminder.analysis() != null) {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("drugName", reminder.analysis().drugName());
            analysis.put("dosage", reminder.analysis().dosage());
            analysis.put("frequency", reminder.analysis().frequency());
            analysis.put("timings", reminder.analysis().timings());
            analysis.put("rawText", reminder.analysis().rawText());
            analysis.put("confidence", reminder.analysis().confidence());
            data.put("analysis", analysis);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private ReminderDto fromReminderDoc(String id, Map<String, Object> data) {
        MedicineAnalysisDto analysis = null;
        Object rawAnalysis = data.get("analysis");
        if (rawAnalysis instanceof Map<?, ?> map) {
            analysis = new MedicineAnalysisDto(
                    (String) map.get("drugName"),
                    (String) map.get("dosage"),
                    (String) map.get("frequency"),
                    map.get("timings") instanceof List<?> list
                            ? list.stream().map(String::valueOf).toList()
                            : List.of(),
                    (String) map.get("rawText"),
                    map.get("confidence") instanceof Number n ? n.doubleValue() : 0.0
            );
        }
        return new ReminderDto(
                id,
                (String) data.get("category"),
                data.get("times") instanceof List<?> list
                        ? list.stream().map(String::valueOf).toList()
                        : List.of(),
                (String) data.get("imageKey"),
                (String) data.get("photoStoragePath"),
                analysis,
                Boolean.TRUE.equals(data.get("confirmed"))
        );
    }

    private static <T> T await(ApiFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Firestore operation failed", e.getCause());
        }
    }
}
