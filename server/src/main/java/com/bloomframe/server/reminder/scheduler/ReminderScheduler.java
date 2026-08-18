package com.bloomframe.server.reminder.scheduler;

import com.bloomframe.server.reminder.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 매 분 정각에 실행
     * 현재 시간에 해당하는 알림을 확인하고 Reminder를 생성한다.
     */
    @Scheduled(cron = "0 * * * * *")
    public void createScheduledReminders() {
        reminderService.createScheduledReminders();
    }
}