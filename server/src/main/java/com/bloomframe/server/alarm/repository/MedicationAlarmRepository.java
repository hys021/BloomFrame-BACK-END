package com.bloomframe.server.alarm.repository;

import com.bloomframe.server.alarm.model.MedicationAlarm;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class MedicationAlarmRepository {

    private static final String COLLECTION = "medicationAlarms";

    private final Firestore firestore;

    public MedicationAlarmRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public MedicationAlarm save(MedicationAlarm alarm) {
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

    // 사용자의 복약 알림 목록 조회
    public List<MedicationAlarm> findAllByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection()
                    .whereEqualTo("userId", userId)
                    .get().get();

            return snapshot.getDocuments()
                    .stream()
                    .map(this::toAlarm)
                    .toList();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<MedicationAlarm> findAllByAlarmTime(String alarmTime) {
        try {
            QuerySnapshot snapshot = collection()
                    .whereEqualTo("alarmTime", alarmTime)
                    .get()
                    .get();

            return snapshot.getDocuments()
                    .stream()
                    .map(this::toAlarm)
                    .toList();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<MedicationAlarm> findById(String alarmId) {
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

    public void update(String alarmId, MedicationAlarm alarm) {
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

    private MedicationAlarm toAlarm(DocumentSnapshot doc) {
        MedicationAlarm alarm = doc.toObject(MedicationAlarm.class);
        if (alarm != null) {
            alarm.setId(doc.getId());
        }
        return alarm;
    }
}
