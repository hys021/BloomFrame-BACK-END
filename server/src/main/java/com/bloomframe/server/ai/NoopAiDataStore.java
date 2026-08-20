package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.AiMedicationDto;
import com.bloomframe.server.ai.dto.NewsletterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoopAiDataStore implements AiDataStore {

    private static final Logger log = LoggerFactory.getLogger(NoopAiDataStore.class);

    private final Map<String, AiMedicationDto> medications = new ConcurrentHashMap<>();
    private final Map<String, Map<String, NewsletterDto>> newsletters = new ConcurrentHashMap<>();

    @Override
    public Optional<String> findUserName(String uid) {
        return Optional.empty();
    }

    @Override
    public Optional<AiMedicationDto> getMedication(String uid, String medicationId) {
        return Optional.ofNullable(medications.computeIfAbsent(uid + ":" + medicationId, key ->
                new AiMedicationDto(medicationId, "", 1, "식후", null, null)));
    }

    @Override
    public List<AiMedicationDto> listMedications(String uid) {
        String prefix = uid + ":";
        List<AiMedicationDto> result = new ArrayList<>();
        medications.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                result.add(value);
            }
        });
        return result;
    }

    @Override
    public AiMedicationDto updateMedication(String uid, String medicationId, AiMedicationDto medication) {
        AiMedicationDto saved = new AiMedicationDto(
                medicationId,
                medication.name(),
                medication.dosePerDay(),
                medication.timing(),
                medication.imageUrl(),
                medication.analysis());
        medications.put(uid + ":" + medicationId, saved);
        log.info("[noop] updateMedication uid={} id={}", uid, medicationId);
        return saved;
    }

    @Override
    public List<String> listDeviceTokens(String uid) {
        return List.of();
    }

    @Override
    public NewsletterDto saveNewsletter(String uid, NewsletterDto newsletter) {
        String id = newsletter.id() != null && !newsletter.id().isBlank()
                ? newsletter.id()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        NewsletterDto saved = newsletter.withId(id);
        newsletters.computeIfAbsent(uid, key -> new ConcurrentHashMap<>()).put(id, saved);
        return saved;
    }

    @Override
    public Optional<NewsletterDto> getNewsletter(String uid, String issueId) {
        Map<String, NewsletterDto> byUser = newsletters.get(uid);
        if (byUser == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUser.get(issueId));
    }

    @Override
    public List<NewsletterDto> listNewsletters(String uid) {
        Map<String, NewsletterDto> byUser = newsletters.get(uid);
        return byUser == null ? List.of() : new ArrayList<>(byUser.values());
    }

    @Override
    public List<NewsletterDto> listDueNewsletters(Instant now) {
        List<NewsletterDto> due = new ArrayList<>();
        for (Map<String, NewsletterDto> byUser : newsletters.values()) {
            for (NewsletterDto newsletter : byUser.values()) {
                if ("pending".equals(newsletter.status())
                        && newsletter.scheduledAt() != null
                        && !newsletter.scheduledAt().isAfter(now)) {
                    due.add(newsletter);
                }
            }
        }
        return due;
    }

    @Override
    public Optional<NewsletterDto> findPendingNewsletterByAlarm(String uid, String alarmId, Instant alarmAt) {
        Map<String, NewsletterDto> byUser = newsletters.get(uid);
        if (byUser == null) {
            return Optional.empty();
        }
        return byUser.values().stream()
                .filter(n -> alarmId.equals(n.alarmId()))
                .filter(n -> "pending".equals(n.status()))
                .filter(n -> n.scheduledAt() != null && n.scheduledAt().equals(alarmAt))
                .findFirst();
    }
}
