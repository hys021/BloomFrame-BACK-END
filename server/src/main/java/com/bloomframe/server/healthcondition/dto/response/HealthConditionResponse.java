package com.bloomframe.server.healthcondition.dto.response;

import com.bloomframe.server.healthcondition.model.HealthCondition;

public record HealthConditionResponse(
        String id,
        String conditionName
) {
    public static HealthConditionResponse from(HealthCondition h) {
        return new HealthConditionResponse(h.getId(), h.getConditionName());
    }
}
