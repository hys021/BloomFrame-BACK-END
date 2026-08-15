package com.bloomframe.server.user.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.user.model.User;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    private static final String COLLECTION = "users";

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    /**
     * email 중복 여부 확인.
     * 주의: Firestore는 UNIQUE 제약이 없어 쿼리 기반으로만 체크합니다.
     * 동시에 같은 이메일로 두 요청이 들어오면 이 체크를 둘 다 통과할 수 있는 race condition이 있습니다.
     * (완전 방지하려면 email을 문서 ID로 쓰는 구조로 바꿔야 합니다)
     */
    public boolean existsByEmail(String email) {
        try {
            QuerySnapshot snapshot = collection().whereEqualTo("email", email).limit(1).get().get();
            return !snapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot snapshot = collection().whereEqualTo("email", email).limit(1).get().get();
            if (snapshot.isEmpty()) {
                return Optional.empty();
            }
            QueryDocumentSnapshot doc = snapshot.getDocuments().get(0);
            return Optional.of(toUser(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<User> findById(String userId) {
        try {
            DocumentSnapshot doc = collection().document(userId).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(toUser(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    /** 신규 사용자 저장. Firestore가 문서 ID를 자동 생성하고, 생성된 ID를 User.id에 채워 반환합니다. */
    public User save(User user) {
        try {
            DocumentReference ref = collection().document(); // ID 자동 생성
            ref.set(user).get();
            user.setId(ref.getId());
            return user;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    /** 특정 필드만 갱신 (마이페이지 수정 등 부분 업데이트용). */
    public void update(String userId, List<String> fields, List<Object> values) {
        try {
            DocumentReference ref = collection().document(userId);
            for (int i = 0; i < fields.size(); i++) {
                ref.update(fields.get(i), values.get(i)).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private User toUser(DocumentSnapshot doc) {
        User user = doc.toObject(User.class);
        if (user != null) {
            user.setId(doc.getId());
        }
        return user;
    }
}
