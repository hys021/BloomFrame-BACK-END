package com.bloomframe.server.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.util.Map;

public class FirebaseFcmService implements FcmService {

    private final FirebaseAppHolder holder;

    public FirebaseFcmService(FirebaseAppHolder holder) {
        this.holder = holder;
    }

    @Override
    public void sendNotification(String token, String title, String body, Map<String, String> data) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());
        if (data != null) {
            builder.putAllData(data);
        }
        try {
            FirebaseMessaging.getInstance(holder.app()).send(builder.build());
        } catch (Exception e) {
            throw new IllegalStateException("FCM send failed", e);
        }
    }
}
