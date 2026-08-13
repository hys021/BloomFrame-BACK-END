package com.bloomframe.server.verification;

import com.bloomframe.server.firebase.FirestoreHolder;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Java #1 소유 컬렉션(users/{uid}/reminders)에 대한 읽기 전용 접근.
 * 필드명은 ReminderSnapshot에 적어둔 것처럼 가정치이므로, 실제 스펙 확정되면
 * parse(...) 메서드 안의 문자열 키만 수정하면 된다.
 */
@Repository
public class FirestoreReminderReader implements ReminderReader {

    private final FirestoreHolder holder;

    public FirestoreReminderReader(FirestoreHolder holder) {
        this.holder = holder;
    }

    @Override
    public Optional<ReminderSnapshot> findById(String uid, String reminderId) {
        if (!holder.enabled()) {
            throw new IllegalStateException("Firestore is disabled — check application-local.yml firebase.credentials-path");
        }
        Firestore firestore = holder.firestore();
        DocumentReference ref = firestore.collection("users").document(uid)
                .collection("reminders").document(reminderId);

        try {
            DocumentSnapshot doc = ref.get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(parse(doc));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore read interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read reminder " + reminderId, e.getCause());
        }
    }

    private ReminderSnapshot parse(DocumentSnapshot doc) {
        // TODO(Java #1 필드 스펙 확정 후): "type", "scheduledAt" 키 이름 검증/수정
        String type = doc.getString("type");
        Timestamp scheduledAt = doc.getTimestamp("scheduledAt");
        if (type == null || scheduledAt == null) {
            throw new IllegalStateException("reminder " + doc.getId() + " missing type/scheduledAt — Java#1 스펙 확인 필요");
        }
        return new ReminderSnapshot(doc.getId(), ReminderType.valueOf(type), scheduledAt.toDate().toInstant());
    }
}