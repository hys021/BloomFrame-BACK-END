package com.bloomframe.server.config;

import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FirebaseAppHolder(FirebaseApp)로부터 Firestore 클라이언트를 꺼내 Bean으로 등록합니다.
 * Firebase가 비활성 상태(firebase.credentials-path 미설정)면 Firestore Bean도 생성하지 않습니다.
 * -> 이 경우 Firestore 의존 클래스(Repository 등) 주입 시 애플리케이션 기동이 실패하니,
 *    로컬에서 firebase-service-account.json 없이 실행할 계획이라면 별도 처리(Optional<Firestore> 등)가 필요합니다.
 */
@Configuration
public class FirestoreConfig {

    @Bean
    Firestore firestore(FirebaseAppHolder holder) {
        if (!holder.enabled()) {
            throw new IllegalStateException(
                    "Firebase가 비활성화되어 있어 Firestore를 사용할 수 없습니다. " +
                    "application-local.yml의 firebase.credentials-path 설정을 확인하세요."
            );
        }
        return FirestoreClient.getFirestore(holder.app());
    }
}
