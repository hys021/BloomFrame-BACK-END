package com.bloomframe.server.healthcondition.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.healthcondition.model.HealthCondition;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class HealthConditionRepository {

    private static final String COLLECTION = "healthConditions";

    private final Firestore firestore;

    public HealthConditionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public HealthCondition save(HealthCondition condition) {
        try {
            DocumentReference ref = collection().document();
            ref.set(condition).get();
            condition.setId(ref.getId());
            return condition;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<HealthCondition> findAllByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection().whereEqualTo("userId", userId).get().get();
            return snapshot.getDocuments().stream().map(this::toCondition).toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<HealthCondition> findById(String conditionId) {
        try {
            DocumentSnapshot doc = collection().document(conditionId).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(toCondition(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void update(String conditionId, HealthCondition condition) {
        try {
            collection().document(conditionId).set(condition, SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void deleteById(String conditionId) {
        try {
            collection().document(conditionId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private HealthCondition toCondition(DocumentSnapshot doc) {
        HealthCondition condition = doc.toObject(HealthCondition.class);
        if (condition != null) {
            condition.setId(doc.getId());
        }
        return condition;
    }
}
