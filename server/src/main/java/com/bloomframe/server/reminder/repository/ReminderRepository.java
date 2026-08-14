package com.bloomframe.server.reminder.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.reminder.model.Reminder;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class ReminderRepository {

    private final Firestore firestore;

    public ReminderRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection(String userId) {
        return firestore.collection("users")
                .document(userId)
                .collection("reminders");
    }

    public Reminder save(Reminder reminder) {
        try {
            DocumentReference ref = collection(reminder.getUserId()).document();

            ref.set(reminder).get();

            reminder.setId(ref.getId());

            return reminder;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<Reminder> findAllByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection(userId)
                    .get()
                    .get();

            return snapshot.getDocuments()
                    .stream()
                    .map(this::toReminder)
                    .toList();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<Reminder> findById(String userId, String reminderId) {
        try {
            DocumentSnapshot doc = collection(userId)
                    .document(reminderId)
                    .get()
                    .get();

            if (!doc.exists()) {
                return Optional.empty();
            }

            return Optional.of(toReminder(doc));

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private Reminder toReminder(DocumentSnapshot doc) {
        Reminder reminder = doc.toObject(Reminder.class);

        if (reminder != null) {
            reminder.setId(doc.getId());
        }

        return reminder;
    }

    public boolean existsByTargetIdAndScheduledAt(
            String userId,
            String targetId,
            Timestamp scheduledAt
    ) {
        try {
            QuerySnapshot snapshot = collection(userId)
                    .whereEqualTo("targetId", targetId)
                    .whereEqualTo("scheduledAt", scheduledAt)
                    .limit(1)
                    .get()
                    .get();

            return !snapshot.isEmpty();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }
}