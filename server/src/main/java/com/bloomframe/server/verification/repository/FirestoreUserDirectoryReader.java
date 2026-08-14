package com.bloomframe.server.verification.repository;

import com.bloomframe.server.firebase.FirestoreHolder;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreUserDirectoryReader implements UserDirectoryReader {

    private final FirestoreHolder holder;

    public FirestoreUserDirectoryReader(FirestoreHolder holder) {
        this.holder = holder;
    }

    @Override
    public List<String> findAllUids() {
        if (!holder.enabled()) {
            throw new IllegalStateException("Firestore is disabled — check application-local.yml firebase.credentials-path");
        }
        Firestore firestore = holder.firestore();
        CollectionReference users = firestore.collection("users");

        try {
            QuerySnapshot snapshot = users.get().get();
            List<String> uids = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                uids.add(doc.getId());
            }
            return uids;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to list users", e.getCause());
        }
    }
}