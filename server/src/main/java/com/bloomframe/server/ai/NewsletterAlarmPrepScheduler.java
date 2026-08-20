package com.bloomframe.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매 분 알람 시각(now+10분)을 조회해 AI newsletter를 선생성한다.
 * ReminderScheduler와 동일한 alarmTime 쿼리만 사용 — 전체 유저 스캔 없음.
 */
@Component
public class NewsletterAlarmPrepScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsletterAlarmPrepScheduler.class);

    private final NewsletterService newsletterService;

    public NewsletterAlarmPrepScheduler(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void pregenerateUpcoming() {
        try {
            newsletterService.pregenerateForUpcomingAlarms();
        } catch (Exception e) {
            log.warn("Newsletter alarm prep failed", e);
        }
    }
}
