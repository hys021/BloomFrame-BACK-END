package com.bloomframe.server.reminder.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.reminder.dto.response.ReminderResponse;
import com.bloomframe.server.reminder.service.ReminderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<ReminderResponse> getReminders(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return reminderService.getReminders(principal.getUserId());
    }

    @GetMapping("/{reminderId}")
    public ReminderResponse getReminder(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String reminderId
    ) {
        return reminderService.getReminder(
                principal.getUserId(),
                reminderId
        );
    }
}