package com.bloomframe.server.firebase;

import com.bloomframe.server.dto.DeviceRegistrationDto;
import com.bloomframe.server.dto.ReminderDto;
import com.bloomframe.server.dto.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory stub when Firebase credentials are not configured.
 * Backend B can develop REST layer; Backend A replaces with FirebaseFirestoreService.
 */
public class NoopFirestoreService implements FirestoreService {

    private static final Logger log = LoggerFactory.getLogger(NoopFirestoreService.class);
    private final Map<String, UserProfileDto> profiles = new ConcurrentHashMap<>();
    private final Map<String, List<ReminderDto>> reminders = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    @Override
    public void saveProfile(String uid, UserProfileDto profile) {
        profiles.put(uid, profile);
        log.info("[noop] saveProfile uid={}", uid);
    }

    @Override
    public Optional<UserProfileDto> getProfile(String uid) {
        return Optional.ofNullable(profiles.get(uid));
    }

    @Override
    public ReminderDto saveReminder(String uid, ReminderDto reminder) {
        ReminderDto saved = reminder.withId(String.valueOf(idSeq.getAndIncrement()));
        reminders.computeIfAbsent(uid, key -> new ArrayList<>()).add(saved);
        log.info("[noop] saveReminder uid={} id={}", uid, saved.id());
        return saved;
    }

    @Override
    public List<ReminderDto> listReminders(String uid) {
        return List.copyOf(reminders.getOrDefault(uid, List.of()));
    }

    @Override
    public ReminderDto updateReminder(String uid, String reminderId, ReminderDto reminder) {
        List<ReminderDto> list = reminders.computeIfAbsent(uid, key -> new ArrayList<>());
        for (int i = 0; i < list.size(); i++) {
            if (reminderId.equals(list.get(i).id())) {
                ReminderDto updated = reminder.withId(reminderId);
                list.set(i, updated);
                log.info("[noop] updateReminder uid={} id={}", uid, reminderId);
                return updated;
            }
        }
        throw new IllegalArgumentException("Reminder not found: " + reminderId);
    }

    @Override
    public void registerDevice(String uid, DeviceRegistrationDto device) {
        log.info("[noop] registerDevice uid={} platform={}", uid, device.platform());
    }
}
