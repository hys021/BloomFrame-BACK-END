package com.bloomframe.server.firebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NoopFcmService implements FcmService {

    private static final Logger log = LoggerFactory.getLogger(NoopFcmService.class);

    @Override
    public void sendNotification(String token, String title, String body, Map<String, String> data) {
        int prefix = Math.min(8, token.length());
        log.info("[noop] FCM title={} token={}...", title, token.substring(0, prefix));
    }
}
