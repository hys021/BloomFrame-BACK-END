package com.bloomframe.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsletterScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsletterScheduler.class);

    private final NewsletterService newsletterService;

    public NewsletterScheduler(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void sendDue() {
        try {
            newsletterService.sendDue();
        } catch (Exception e) {
            log.warn("Newsletter scheduler failed", e);
        }
    }
}
