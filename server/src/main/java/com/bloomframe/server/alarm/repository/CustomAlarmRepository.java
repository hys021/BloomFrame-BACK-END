package com.bloomframe.server.alarm.repository;

import com.bloomframe.server.alarm.model.CustomAlarm;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class CustomAlarmRepository {

    private static final String COLLECTION = "customAlarms";

    private final Firestore firestore;

    public CustomAlarmRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public CustomAlarm save(CustomAlarm alarm) {
        try {
            DocumentReference ref = collection().document();
            ref.set(alarm).get();
            alarm.setId(ref.getId());
            return alarm;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<CustomAlarm> findAllByUserIdOrderByAlarmTime(String userId) {
        try {
            QuerySnapshot snapshot = collection()
                    .whereEqualTo("userId", userId)
                    .orderBy("alarmTime", Query.Direction.ASCENDING)
                    .get().get();
            return snapshot.getDocuments().stream().map(this::toAlarm).toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<CustomAlarm> findById(String alarmId) {
        try {
            DocumentSnapshot doc = collection().document(alarmId).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(toAlarm(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void update(String alarmId, CustomAlarm alarm) {
        try {
            collection().document(alarmId).set(alarm, SetOptions.merge()).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void deleteById(String alarmId) {
        try {
            collection().document(alarmId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private CustomAlarm toAlarm(DocumentSnapshot doc) {
        CustomAlarm alarm = doc.toObject(CustomAlarm.class);
        if (alarm != null) {
            alarm.setId(doc.getId());
        }
        return alarm;
    }
}
