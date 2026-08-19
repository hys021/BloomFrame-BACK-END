package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.AiMedicationDto;
import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.NewsletterDto;
import com.bloomframe.server.firebase.FirestoreHolder;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FirestoreAiDataStore implements AiDataStore {

    private static final String MEDICATIONS = "medications";
    private static final String HEALTH_CONDITIONS = "healthConditions";

    private final Firestore firestore;

    public FirestoreAiDataStore(FirestoreHolder holder) {
        this.firestore = holder.firestore();
    }

    @Override
    public Optional<String> findUserName(String uid) {
        DocumentSnapshot doc = await(firestore.collection("users").document(uid).get());
        if (!doc.exists()) {
            return Optional.empty();
        }
        String name = doc.getString("name");
        return name == null || name.isBlank() ? Optional.empty() : Optional.of(name);
    }

    @Override
    public Optional<AiMedicationDto> getMedication(String uid, String medicationId) {
        DocumentSnapshot doc = await(firestore.collection(MEDICATIONS).document(medicationId).get());
        if (!doc.exists()) {
            return Optional.empty();
        }
        AiMedicationDto medication = fromMedicationDoc(doc.getId(), doc.getData());
        if (!uid.equals(doc.getString("userId"))) {
            return Optional.empty();
        }
        return Optional.of(medication);
    }

    @Override
    public List<AiMedicationDto> listMedications(String uid) {
        QuerySnapshot snapshot = await(
                firestore.collection(MEDICATIONS).whereEqualTo("userId", uid).get());
        List<AiMedicationDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            result.add(fromMedicationDoc(doc.getId(), doc.getData()));
        }
        return result;
    }

    @Override
    public AiMedicationDto updateMedication(String uid, String medicationId, AiMedicationDto medication) {
        Optional<AiMedicationDto> existing = getMedication(uid, medicationId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Medication not found: " + medicationId);
        }
        DocumentReference ref = firestore.collection(MEDICATIONS).document(medicationId);
        await(ref.set(toMedicationPatch(medication), SetOptions.merge()));
        return medication;
    }

    @Override
    public AiMedicationDto createMedication(String uid, AiMedicationDto medication) {
        DocumentReference ref = firestore.collection(MEDICATIONS).document();
        AiMedicationDto toSave = new AiMedicationDto(
                ref.getId(),
                medication.name(),
                medication.dosePerDay(),
                medication.timing(),
                medication.imageUrl(),
                medication.analysis());
        Map<String, Object> data = toMedicationPatch(toSave);
        data.put("userId", uid);
        await(ref.set(data));
        return toSave;
    }

    @Override
    public List<String> listHealthConditionNames(String uid) {
        QuerySnapshot snapshot = await(
                firestore.collection(HEALTH_CONDITIONS).whereEqualTo("userId", uid).get());
        List<String> names = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String name = doc.getString("conditionName");
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    @Override
    public List<String> listDeviceTokens(String uid) {
        List<String> tokens = new ArrayList<>();
        DocumentSnapshot user = await(firestore.collection("users").document(uid).get());
        if (user.exists()) {
            addToken(tokens, user.getString("fcmToken"));
        }
        QuerySnapshot snapshot = await(
                firestore.collection("users").document(uid).collection("devices").get());
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            addToken(tokens, doc.getString("fcmToken"));
        }
        return tokens;
    }

    @Override
    public NewsletterDto saveNewsletter(String uid, NewsletterDto newsletter) {
        String id = newsletter.id() != null && !newsletter.id().isBlank()
                ? newsletter.id()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        NewsletterDto saved = newsletter.withId(id);
        await(firestore.collection("users").document(uid)
                .collection("newsletters").document(id).set(toNewsletterMap(saved)));
        return saved;
    }

    @Override
    public Optional<NewsletterDto> getNewsletter(String uid, String issueId) {
        DocumentSnapshot doc = await(firestore.collection("users").document(uid)
                .collection("newsletters").document(issueId).get());
        if (!doc.exists()) {
            return Optional.empty();
        }
        return Optional.of(fromNewsletterDoc(uid, doc.getId(), doc.getData()));
    }

    @Override
    public List<NewsletterDto> listNewsletters(String uid) {
        QuerySnapshot snapshot = await(
                firestore.collection("users").document(uid).collection("newsletters").get());
        List<NewsletterDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            result.add(fromNewsletterDoc(uid, doc.getId(), doc.getData()));
        }
        return result;
    }

    @Override
    public List<NewsletterDto> listDueNewsletters(Instant now) {
        QuerySnapshot snapshot;
        try {
            snapshot = await(
                    firestore.collectionGroup("newsletters").whereEqualTo("status", "pending").get());
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Failed to query pending newsletters (collection group). Check Firestore indexes.", e);
        }
        List<NewsletterDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            String uid = doc.getString("uid");
            if (uid == null || uid.isBlank()) {
                DocumentReference parent = doc.getReference().getParent().getParent();
                uid = parent != null ? parent.getId() : "";
            }
            NewsletterDto newsletter = fromNewsletterDoc(uid, doc.getId(), doc.getData());
            if (newsletter.scheduledAt() != null && !newsletter.scheduledAt().isAfter(now)) {
                result.add(newsletter);
            }
        }
        return result;
    }

    private static void addToken(List<String> tokens, String token) {
        if (token != null && !token.isBlank() && !tokens.contains(token)) {
            tokens.add(token);
        }
    }

    private Map<String, Object> toMedicationPatch(AiMedicationDto medication) {
        Map<String, Object> data = new HashMap<>();
        if (medication.name() != null && !medication.name().isBlank()) {
            data.put("name", medication.name());
        }
        if (medication.dosePerDay() != null) {
            data.put("dosePerDay", medication.dosePerDay());
        }
        if (medication.timing() != null && !medication.timing().isBlank()) {
            data.put("timing", medication.timing());
        }
        if (medication.imageUrl() != null) {
            data.put("imageUrl", medication.imageUrl());
        }
        data.put("updatedAt", com.google.cloud.Timestamp.now());
        if (medication.analysis() != null) {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("drugName", medication.analysis().drugName());
            analysis.put("dosage", medication.analysis().dosage());
            analysis.put("frequency", medication.analysis().frequency());
            analysis.put("dosePerDay", medication.analysis().dosePerDay());
            analysis.put("timing", medication.analysis().timing());
            analysis.put("timings", medication.analysis().timings());
            analysis.put("rawText", medication.analysis().rawText());
            analysis.put("confidence", medication.analysis().confidence());
            data.put("analysis", analysis);
        }
        return data;
    }

    private AiMedicationDto fromMedicationDoc(String id, Map<String, Object> data) {
        MedicineAnalysisDto analysis = null;
        Object rawAnalysis = data.get("analysis");
        if (rawAnalysis instanceof Map<?, ?> map) {
            analysis = new MedicineAnalysisDto(
                    string(map.get("drugName")),
                    string(map.get("dosage")),
                    string(map.get("frequency")),
                    map.get("dosePerDay") instanceof Number n ? n.intValue() : null,
                    string(map.get("timing")),
                    map.get("timings") instanceof List<?> list
                            ? list.stream().map(String::valueOf).toList()
                            : List.of(),
                    string(map.get("rawText")),
                    map.get("confidence") instanceof Number n ? n.doubleValue() : 0.0
            );
        }
        Integer dosePerDay = data.get("dosePerDay") instanceof Number n ? n.intValue() : null;
        return new AiMedicationDto(
                id,
                string(data.get("name")),
                dosePerDay,
                string(data.get("timing")),
                string(data.get("imageUrl")),
                analysis
        );
    }

    private static String string(Object value) {
        return value instanceof String s ? s : null;
    }

    private Map<String, Object> toNewsletterMap(NewsletterDto newsletter) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", newsletter.uid());
        data.put("trigger", newsletter.trigger());
        data.put("reminderId", newsletter.reminderId());
        data.put("kind", newsletter.kind());
        data.put("title", newsletter.title());
        data.put("body", newsletter.body());
        data.put("tips", newsletter.tips());
        data.put("status", newsletter.status());
        data.put("scheduledAt", toTimestamp(newsletter.scheduledAt()));
        data.put("sentAt", toTimestamp(newsletter.sentAt()));
        data.put("updatedAt", com.google.cloud.Timestamp.now());
        return data;
    }

    private NewsletterDto fromNewsletterDoc(String uid, String id, Map<String, Object> data) {
        Object tipsRaw = data.get("tips");
        List<String> tips = tipsRaw instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();
        String docUid = data.get("uid") instanceof String s && !s.isBlank() ? s : uid;
        return new NewsletterDto(
                id,
                docUid,
                (String) data.get("trigger"),
                (String) data.get("reminderId"),
                (String) data.get("kind"),
                (String) data.get("title"),
                (String) data.get("body"),
                tips,
                (String) data.get("status"),
                toInstant(data.get("scheduledAt")),
                toInstant(data.get("sentAt"))
        );
    }

    private static com.google.cloud.Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : com.google.cloud.Timestamp.of(java.util.Date.from(instant));
    }

    private static Instant toInstant(Object value) {
        if (value instanceof com.google.cloud.Timestamp timestamp) {
            return timestamp.toDate().toInstant();
        }
        return null;
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
