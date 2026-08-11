package com.bloomframe.server.controller;

import com.bloomframe.server.dto.DeviceRegistrationDto;
import com.bloomframe.server.dto.ReminderDto;
import com.bloomframe.server.firebase.FirestoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{uid}")
public class ReminderController {

    private final FirestoreService firestoreService;

    public ReminderController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping("/reminders")
    public List<ReminderDto> listReminders(@PathVariable String uid) {
        return firestoreService.listReminders(uid);
    }

    @PostMapping("/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderDto createReminder(@PathVariable String uid, @Valid @RequestBody ReminderDto reminder) {
        return firestoreService.saveReminder(uid, reminder);
    }

    @PatchMapping("/reminders/{id}")
    public ReminderDto updateReminder(
            @PathVariable String uid,
            @PathVariable String id,
            @Valid @RequestBody ReminderDto reminder) {
        return firestoreService.updateReminder(uid, id, reminder);
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerDevice(@PathVariable String uid, @Valid @RequestBody DeviceRegistrationDto device) {
        firestoreService.registerDevice(uid, device);
    }
}
