package com.bloomframe.server.controller;

import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.bloomframe.server.storage.StorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/firebase")
public class FirebaseHealthController {

    private final FirebaseAppHolder holder;
    private final StorageService storageService;

    public FirebaseHealthController(FirebaseAppHolder holder, StorageService storageService) {
        this.holder = holder;
        this.storageService = storageService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firebase", holder.enabled() ? "ok" : "disabled");
        body.put("storage", storageService.isConfigured() ? "r2" : "noop");
        if (holder.projectId() != null) {
            body.put("projectId", holder.projectId());
        }
        return body;
    }
}
