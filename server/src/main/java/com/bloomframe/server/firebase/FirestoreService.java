package com.bloomframe.server.firebase;

import com.bloomframe.server.dto.DeviceRegistrationDto;
import com.bloomframe.server.dto.ReminderDto;
import com.bloomframe.server.dto.UserProfileDto;

import java.util.List;
import java.util.Optional;

public interface FirestoreService {

    void saveProfile(String uid, UserProfileDto profile);

    Optional<UserProfileDto> getProfile(String uid);

    ReminderDto saveReminder(String uid, ReminderDto reminder);

    List<ReminderDto> listReminders(String uid);

    ReminderDto updateReminder(String uid, String reminderId, ReminderDto reminder);

    void registerDevice(String uid, DeviceRegistrationDto device);
}
