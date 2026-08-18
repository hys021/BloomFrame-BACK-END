package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.NewsletterGenerateRequest;
import com.bloomframe.server.verification.event.AuthCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuthCompletedNewsletterListener {

    private static final Logger log = LoggerFactory.getLogger(AuthCompletedNewsletterListener.class);

    private final NewsletterService newsletterService;

    public AuthCompletedNewsletterListener(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @Async
    @EventListener
    public void onAuthCompleted(AuthCompletedEvent event) {
        try {
            newsletterService.generate(
                    event.uid(),
                    new NewsletterGenerateRequest(event.reminderId(), "alarm_dismiss", null));
        } catch (Exception e) {
            log.warn("Newsletter after auth-touch failed uid={} reminderId={}",
                    event.uid(), event.reminderId(), e);
        }
    }
}
