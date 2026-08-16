package com.bloomframe.server.sync.service;

import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.sync.model.SyncSnapshot;
import com.bloomframe.server.verification.service.AuthTouchService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SyncService {

    private final PlantStateService plantStateService;
    private final AuthTouchService authTouchService;

    public SyncService(PlantStateService plantStateService, AuthTouchService authTouchService) {
        this.plantStateService = plantStateService;
        this.authTouchService = authTouchService;
    }

    public SyncSnapshot getSnapshot(String uid, Instant from, Instant to) {
        return new SyncSnapshot(
                plantStateService.getState(uid),
                authTouchService.getLogs(uid, from, to)
        );
    }
}