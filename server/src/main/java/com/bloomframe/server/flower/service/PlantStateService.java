package com.bloomframe.server.flower.service;

import com.bloomframe.server.flower.model.PlantState;
import com.bloomframe.server.flower.model.PlantStatus;
import com.bloomframe.server.flower.repository.PlantStateRepository;
import org.springframework.stereotype.Service;

@Service
public class PlantStateService {

    private final PlantStateRepository repository;

    public PlantStateService(PlantStateRepository repository) {
        this.repository = repository;
    }

    public PlantState getState(String uid) {
        return repository.find(uid);
    }

    public void markBlooming(String uid) {
        repository.save(uid, PlantStatus.BLOOMING);
    }

    public void markWilted(String uid) {
        repository.save(uid, PlantStatus.WILTED);
    }
}