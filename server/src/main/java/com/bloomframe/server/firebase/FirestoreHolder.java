package com.bloomframe.server.firebase;

import com.google.cloud.firestore.Firestore;

/**
 * FirebaseAppHolder와 동일한 패턴의 Firestore 클라이언트 홀더.
 * Firebase가 비활성화된 로컬 환경에서도 앱이 죽지 않도록 disabled 상태를 허용한다.
 */
public record FirestoreHolder(boolean enabled, Firestore firestore) {

    public static FirestoreHolder disabled() {
        return new FirestoreHolder(false, null);
    }

    public static FirestoreHolder enabled(Firestore firestore) {
        return new FirestoreHolder(true, firestore);
    }
}