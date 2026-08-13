package com.bloomframe.server.user.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.user.model.PhoneVerification;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * "phoneVerifications" 컬렉션. 문서 ID로 phone(전화번호)을 그대로 사용합니다.
 * -> 같은 번호로 재요청(재발송) 시 새 문서를 만드는 대신 같은 문서를 덮어써서,
 *    "최신 코드만 유효하다" 정책을 자연스럽게 구현합니다 (SQL 버전의 deleteByPhone+insert와 동일 효과).
 */
@Repository
public class PhoneVerificationRepository {

    private static final String COLLECTION = "phoneVerifications";

    private final Firestore firestore;

    public PhoneVerificationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public void save(PhoneVerification verification) {
        try {
            collection().document(verification.getPhone()).set(verification).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<PhoneVerification> findByPhone(String phone) {
        try {
            DocumentSnapshot doc = collection().document(phone).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            PhoneVerification verification = doc.toObject(PhoneVerification.class);
            if (verification != null) {
                verification.setPhone(phone);
            }
            return Optional.of(verification);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void markVerified(String phone) {
        try {
            DocumentReference ref = collection().document(phone);
            ref.update("verified", true).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    /** 회원가입 완료 후 인증 정보 삭제 (설계 문서 명시 사항). */
    public void deleteByPhone(String phone) {
        try {
            collection().document(phone).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }
}
