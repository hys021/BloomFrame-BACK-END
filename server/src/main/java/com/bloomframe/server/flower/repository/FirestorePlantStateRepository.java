package com.bloomframe.server.flower.repository;

import com.bloomframe.server.firebase.FirestoreHolder;
import com.bloomframe.server.flower.model.PlantState;
import com.bloomframe.server.flower.model.PlantStatus;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestorePlantStateRepository implements PlantStateRepository {

    private final FirestoreHolder holder;
    private final Clock clock;

    public FirestorePlantStateRepository(FirestoreHolder holder, Clock clock) {
        this.holder = holder;
        this.clock = clock;
    }

    private DocumentReference doc(Firestore firestore, String uid) {
        // users/{uid}/plantState/current — plantState를 고정 id 문서 하나로 관리
        return firestore.collection("users").document(uid).collection("plantState").document("current");
    }

    @Override
    public PlantState find(String uid) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        try {
            DocumentSnapshot snapshot = doc(firestore, uid).get().get();
            if (!snapshot.exists()) {
                return PlantState.defaultState(clock.instant());
            }
            String status = snapshot.getString("status");
            Timestamp updatedAt = snapshot.getTimestamp("updatedAt");
            return new PlantState(
                    PlantStatus.valueOf(status),
                    updatedAt != null ? updatedAt.toDate().toInstant() : clock.instant()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read plantState for uid " + uid, e.getCause());
        }
    }

    @Override
    public void save(String uid, PlantStatus status) {
        requireEnabled();
        Firestore firestore = holder.firestore();

        Instant now = clock.instant();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status.name());
        data.put("updatedAt", Timestamp.ofTimeSecondsAndNanos(now.getEpochSecond(), now.getNano()));

        try {
            doc(firestore, uid).set(data).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore write interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save plantState for uid " + uid, e.getCause());
        }
    }

    private void requireEnabled() {
        if (!holder.enabled()) {
            throw new IllegalStateException("Firestore is disabled — check application-local.yml firebase.credentials-path");
        }
    }
}