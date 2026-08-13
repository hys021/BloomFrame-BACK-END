package com.bloomframe.server.healthcondition.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.healthcondition.dto.request.HealthConditionRequest;
import com.bloomframe.server.healthcondition.dto.response.HealthConditionResponse;
import com.bloomframe.server.healthcondition.model.HealthCondition;
import com.bloomframe.server.healthcondition.repository.HealthConditionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthConditionService {

    private final HealthConditionRepository healthConditionRepository;

    public HealthConditionService(HealthConditionRepository healthConditionRepository) {
        this.healthConditionRepository = healthConditionRepository;
    }

    public List<HealthConditionResponse> getConditions(String userId) {
        return healthConditionRepository.findAllByUserId(userId).stream()
                .map(HealthConditionResponse::from)
                .toList();
    }

    public HealthConditionResponse register(String userId, HealthConditionRequest request) {
        HealthCondition condition = new HealthCondition(userId, request.conditionName());
        healthConditionRepository.save(condition);
        return HealthConditionResponse.from(condition);
    }

    public HealthConditionResponse update(String userId, String conditionId, HealthConditionRequest request) {
        HealthCondition condition = findOwned(userId, conditionId);
        condition.setConditionName(request.conditionName());
        healthConditionRepository.update(conditionId, condition);
        return HealthConditionResponse.from(condition);
    }

    public void delete(String userId, String conditionId) {
        findOwned(userId, conditionId); // 소유권 검증 먼저
        healthConditionRepository.deleteById(conditionId);
    }

    private HealthCondition findOwned(String userId, String conditionId) {
        HealthCondition condition = healthConditionRepository.findById(conditionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HEALTH_CONDITION_NOT_FOUND));

        if (!condition.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.HEALTH_CONDITION_FORBIDDEN);
        }
        return condition;
    }
}
