package com.bloomframe.server.flower.repository;

import com.bloomframe.server.flower.model.PlantState;
import com.bloomframe.server.flower.model.PlantStatus;

public interface PlantStateRepository {

    /** 문서가 없으면 PlantState.defaultState(...)를 반환한다. */
    PlantState find(String uid);

    void save(String uid, PlantStatus status);
}