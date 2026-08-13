package com.bloomframe.server.medication.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.medication.model.Medication;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class MedicationRepository {

    private static final String COLLECTION = "medications";

    private final Firestore firestore;

    public MedicationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public Medication save(Medication medication) {
        try {
            DocumentReference ref = collection().document(); // ID 자동 생성
            ref.set(medication).get();
            medication.setId(ref.getId());
            return medication;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<Medication> findAllByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection().whereEqualTo("userId", userId).get().get();
            return snapshot.getDocuments().stream().map(this::toMedication).toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<Medication> findById(String medicationId) {
        try {
            DocumentSnapshot doc = collection().document(medicationId).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(toMedication(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void update(String medicationId, Medication medication) {
        try {
            DocumentReference ref = collection().document(medicationId);
            ref.set(medication, com.google.cloud.firestore.SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void deleteById(String medicationId) {
        try {
            collection().document(medicationId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private Medication toMedication(DocumentSnapshot doc) {
        Medication medication = doc.toObject(Medication.class);
        if (medication != null) {
            medication.setId(doc.getId());
        }
        return medication;
    }
}
