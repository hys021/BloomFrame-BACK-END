package com.bloomframe.server.sync.controller;

import com.bloomframe.server.common.security.AuthenticatedUid;
import com.bloomframe.server.sync.model.SyncSnapshot;
import com.bloomframe.server.sync.service.SyncService;
import com.bloomframe.server.verification.exception.UidMismatchException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
public class SyncController {

    private static final Duration DEFAULT_LOOKBACK = Duration.ofHours(24);

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/api/v1/users/{uid}/sync")
    public SyncSnapshot sync(
            @PathVariable String uid,
            @AuthenticatedUid String authenticatedUid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (!uid.equals(authenticatedUid)) {
            throw new UidMismatchException();
        }
        Instant now = Instant.now();
        Instant rangeTo = (to != null) ? to : now;
        Instant rangeFrom = (from != null) ? from : rangeTo.minus(DEFAULT_LOOKBACK);

        return syncService.getSnapshot(uid, rangeFrom, rangeTo);
    }
}