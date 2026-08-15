package com.bloomframe.server.ai.dto;

import java.time.Instant;

public record NewsletterGenerateRequest(
        String reminderId,
        String trigger,
        Instant scheduledAt
) {}
