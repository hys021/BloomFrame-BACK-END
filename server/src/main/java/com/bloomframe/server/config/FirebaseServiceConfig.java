package com.bloomframe.server.config;

import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.bloomframe.server.firebase.FirebaseFcmService;
import com.bloomframe.server.firebase.FirebaseFirestoreService;
import com.bloomframe.server.firebase.FcmService;
import com.bloomframe.server.firebase.FirestoreService;
import com.bloomframe.server.firebase.NoopFcmService;
import com.bloomframe.server.firebase.NoopFirestoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseServiceConfig {

    @Bean
    FirestoreService firestoreService(FirebaseAppHolder holder) {
        if (holder.enabled()) {
            return new FirebaseFirestoreService(holder);
        }
        return new NoopFirestoreService();
    }

    @Bean
    FcmService fcmService(FirebaseAppHolder holder) {
        if (holder.enabled()) {
            return new FirebaseFcmService(holder);
        }
        return new NoopFcmService();
    }
}
