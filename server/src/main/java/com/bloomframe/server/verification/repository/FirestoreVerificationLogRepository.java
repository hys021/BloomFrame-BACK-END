package com.bloomframe.server.verification.repository;

import com.bloomframe.server.firebase.FirestoreHolder;
import com.bloomframe.server.verification.model.AlertStage;
import com.bloomframe.server.verification.model.ReminderType;
import com.bloomframe.server.verification.model.VerificationLog;
import com.bloomframe.server.verification.model.VerificationStatus;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class FirestoreVerificationLogRepository implements VerificationLogRepository {

    private final FirestoreHolder holder;

    public FirestoreVerificationLogRepository(FirestoreHolder holder) {
        this.holder = holder;
    }

    private CollectionReference collection(Firestore firestore, String uid) {
        return firestore.collection("users").document(uid).collection("verificationLogs");
    }

    @Override
    public String save(String uid, VerificationLog log) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("type", log.type().name());
        doc.put("targetId", log.targetId());
        doc.put("alertStage", log.alertStage().order());
        doc.put("status", log.status().name());
        doc.put("scheduledAt", toTimestamp(log.scheduledAt()));
        doc.put("verifiedAt", log.verifiedAt() != null ? toTimestamp(log.verifiedAt()) : null);

        try {
            DocumentReference ref = collection(firestore, uid).document();
            ref.set(doc).get();
            return ref.getId();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore write interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save verification log", e.getCause());
        }
    }

    @Override
    public List<VerificationLog> findByScheduledAtRange(String uid, Instant from, Instant to) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        try {
            QuerySnapshot snapshot = collection(firestore, uid)
                    .whereGreaterThanOrEqualTo("scheduledAt", toTimestamp(from))
                    .whereLessThanOrEqualTo("scheduledAt", toTimestamp(to))
                    .orderBy("scheduledAt")
                    .get()
                    .get();

            return snapshot.getDocuments().stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to query verification logs", e.getCause());
        }
    }

    @Override
    public boolean existsForReminderOccurrence(String uid, String targetId, Instant scheduledAt) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        try {
            QuerySnapshot snapshot = collection(firestore, uid)
                    .whereEqualTo("targetId", targetId)
                    .whereEqualTo("scheduledAt", toTimestamp(scheduledAt))
                    .limit(1)
                    .get()
                    .get();
            return !snapshot.isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to check existing log for " + targetId, e.getCause());
        }
    }

    @Override
    public boolean existsSuccessForReminderOccurrence(String uid, String targetId, Instant scheduledAt) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        try {
            QuerySnapshot snapshot = collection(firestore, uid)
                    .whereEqualTo("targetId", targetId)
                    .whereEqualTo("scheduledAt", toTimestamp(scheduledAt))
                    .whereEqualTo("status", VerificationStatus.SUCCESS.name())
                    .limit(1)
                    .get()
                    .get();
            return !snapshot.isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to check success log for " + targetId, e.getCause());
        }
    }

    private VerificationLog toDomain(QueryDocumentSnapshot doc) {
        Timestamp scheduledAt = doc.getTimestamp("scheduledAt");
        Timestamp verifiedAt = doc.getTimestamp("verifiedAt");
        return new VerificationLog(
                doc.getId(),
                ReminderType.valueOf(doc.getString("type")),
                doc.getString("targetId"),
                doc.getLong("alertStage") == 1 ? AlertStage.FIRST : AlertStage.SECOND,
                VerificationStatus.valueOf(doc.getString("status")),
                scheduledAt != null ? scheduledAt.toDate().toInstant() : null,
                verifiedAt != null ? verifiedAt.toDate().toInstant() : null
        );
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    private void requireEnabled() {
        if (!holder.enabled()) {
            throw new IllegalStateException("Firestore is disabled — check application-local.yml firebase.credentials-path");
        }
    }
}