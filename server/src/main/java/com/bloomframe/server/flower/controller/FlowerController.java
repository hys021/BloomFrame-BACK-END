package com.bloomframe.server.flower.controller;

import com.bloomframe.server.common.security.AuthenticatedUid;
import com.bloomframe.server.flower.model.PlantState;
import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.verification.exception.UidMismatchException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlowerController {

    private final PlantStateService plantStateService;

    public FlowerController(PlantStateService plantStateService) {
        this.plantStateService = plantStateService;
    }

    @GetMapping("/api/v1/users/{uid}/flower")
    public PlantState getFlower(@PathVariable String uid, @AuthenticatedUid String authenticatedUid) {
        if (!uid.equals(authenticatedUid)) {
            throw new UidMismatchException();
        }
        return plantStateService.getState(uid);
    }
}