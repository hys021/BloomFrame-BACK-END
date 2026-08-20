package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.AiMedicationDto;
import com.bloomframe.server.ai.dto.NewsletterDto;
import com.bloomframe.server.ai.dto.NewsletterGenerateRequest;
import com.bloomframe.server.alarm.repository.CustomAlarmRepository;
import com.bloomframe.server.alarm.repository.ExerciseAlarmRepository;
import com.bloomframe.server.alarm.repository.MedicationAlarmRepository;
import com.bloomframe.server.firebase.FcmService;
import com.bloomframe.server.reminder.model.Reminder;
import com.bloomframe.server.reminder.repository.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final int ALARM_PREP_MINUTES = 10;

    private final AiClient aiClient;
    private final AiDataStore aiDataStore;
    private final FcmService fcmService;
    private final MedicationAlarmRepository medicationAlarmRepository;
    private final ExerciseAlarmRepository exerciseAlarmRepository;
    private final CustomAlarmRepository customAlarmRepository;
    private final ReminderRepository reminderRepository;

    public NewsletterService(
            AiClient aiClient,
            AiDataStore aiDataStore,
            FcmService fcmService,
            MedicationAlarmRepository medicationAlarmRepository,
            ExerciseAlarmRepository exerciseAlarmRepository,
            CustomAlarmRepository customAlarmRepository,
            ReminderRepository reminderRepository
    ) {
        this.aiClient = aiClient;
        this.aiDataStore = aiDataStore;
        this.fcmService = fcmService;
        this.medicationAlarmRepository = medicationAlarmRepository;
        this.exerciseAlarmRepository = exerciseAlarmRepository;
        this.customAlarmRepository = customAlarmRepository;
        this.reminderRepository = reminderRepository;
    }

    public NewsletterDto generate(String uid, NewsletterGenerateRequest request) {
        NewsletterGenerateRequest req = request == null
                ? new NewsletterGenerateRequest(null, "manual", null)
                : request;
        Instant scheduledAt = req.scheduledAt() != null ? req.scheduledAt() : Instant.now();
        boolean sendNow = !scheduledAt.isAfter(Instant.now().plusSeconds(5));

        String kind = resolveKind(uid, req.reminderId());
        String title = "";
        String body = "";
        List<String> tips = List.of();
        if (sendNow) {
            NewsletterContent content = aiClient.generateNewsletter(
                    context(uid, req.reminderId(), req.trigger(), kind, scheduledAt));
            title = content.title();
            body = content.body();
            tips = content.tips();
        }

        String trigger = req.trigger() == null || req.trigger().isBlank() ? "manual" : req.trigger();
        NewsletterDto saved = aiDataStore.saveNewsletter(uid, new NewsletterDto(
                newId(),
                uid,
                trigger,
                req.reminderId(),
                null,
                kind,
                title,
                body,
                tips,
                "pending",
                scheduledAt,
                null
        ));
        if (sendNow) {
            return send(uid, saved.id());
        }
        return saved;
    }

    /**
     * 매 분 실행 — 알람 시각 10분 전에 해당하는 알람을 조회해 AI newsletter를 선생성한다.
     * Reminder와 무관하게 alarmId + scheduledAt으로만 식별한다.
     */
    public void pregenerateForUpcomingAlarms() {
        ZonedDateTime alarmTime = ZonedDateTime.now(ZONE)
                .plusMinutes(ALARM_PREP_MINUTES)
                .withSecond(0)
                .withNano(0);
        String alarmTimeStr = alarmTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        Instant alarmInstant = alarmTime.toInstant();

        medicationAlarmRepository.findAllByAlarmTime(alarmTimeStr)
                .forEach(alarm -> pregenerateForAlarm(alarm.getUserId(), alarm.getId(), alarmInstant));

        exerciseAlarmRepository.findAllByAlarmTime(alarmTimeStr)
                .forEach(alarm -> pregenerateForAlarm(alarm.getUserId(), alarm.getId(), alarmInstant));

        customAlarmRepository.findAllByAlarmTime(alarmTimeStr)
                .forEach(alarm -> pregenerateForAlarm(alarm.getUserId(), alarm.getId(), alarmInstant));
    }

    public NewsletterDto pregenerateForAlarm(String uid, String alarmId, Instant alarmAt) {
        Optional<NewsletterDto> existing = aiDataStore.findPendingNewsletterByAlarm(uid, alarmId, alarmAt);
        if (existing.isPresent() && hasContent(existing.get())) {
            return existing.get();
        }

        String kind = resolveKind(uid, null);
        NewsletterContent content = aiClient.generateNewsletter(
                context(uid, null, "alarm_prep", kind, alarmAt));
        NewsletterDto saved = aiDataStore.saveNewsletter(uid, new NewsletterDto(
                newId(),
                uid,
                "alarm_prep",
                null,
                alarmId,
                kind,
                content.title(),
                content.body(),
                content.tips(),
                "pending",
                alarmAt,
                null
        ));
        log.info("Newsletter pregenerated uid={} alarmId={} issueId={}", uid, alarmId, saved.id());
        return saved;
    }

    /**
     * 터치 인증 후 — Reminder의 targetId(alarmId) + scheduledAt으로 선생성본을 찾아 발송한다.
     */
    public NewsletterDto sendAfterAuth(String uid, String reminderId) {
        Optional<Reminder> reminder = reminderRepository.findById(uid, reminderId);
        if (reminder.isPresent()) {
            Instant alarmAt = Instant.ofEpochSecond(
                    reminder.get().getScheduledAt().getSeconds(),
                    reminder.get().getScheduledAt().getNanos());
            Optional<NewsletterDto> pending = aiDataStore.findPendingNewsletterByAlarm(
                    uid, reminder.get().getTargetId(), alarmAt);
            if (pending.isPresent() && hasContent(pending.get())) {
                return send(uid, pending.get().id());
            }
        }
        log.info("No pregenerated newsletter uid={} reminderId={} — generating on auth", uid, reminderId);
        return generate(uid, new NewsletterGenerateRequest(reminderId, "alarm_dismiss", null));
    }

    public List<NewsletterDto> list(String uid) {
        return aiDataStore.listNewsletters(uid);
    }

    public NewsletterDto send(String uid, String issueId) {
        NewsletterDto existing = aiDataStore.getNewsletter(uid, issueId)
                .orElseThrow(() -> new IllegalArgumentException("Newsletter not found: " + issueId));
        if ("sent".equals(existing.status())) {
            return existing;
        }

        NewsletterDto ready = existing;
        if (existing.title() == null || existing.title().isBlank()) {
            String kind = resolveKind(uid, existing.reminderId());
            NewsletterContent content = aiClient.generateNewsletter(
                    context(uid, existing.reminderId(), existing.trigger(), kind, existing.scheduledAt()));
            ready = existing.withKindAndContent(kind, content.title(), content.body(), content.tips());
        }

        String pushTitle = truncate(ready.title(), 40);
        String pushBody = truncate(ready.body(), 120);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "newsletter");
        data.put("issueId", issueId);
        data.put("kind", nullToEmpty(ready.kind()));
        data.put("title", nullToEmpty(pushTitle));
        data.put("body", pushBody);

        List<String> tokens = aiDataStore.listDeviceTokens(uid);
        if (tokens.isEmpty()) {
            log.info("No FCM tokens for uid={} — newsletter {} stored for in-app fetch", uid, issueId);
            return aiDataStore.saveNewsletter(uid, ready.withSent(Instant.now()));
        }

        int delivered = 0;
        for (String token : tokens) {
            try {
                fcmService.sendNotification(token, pushTitle, pushBody, data);
                delivered++;
            } catch (Exception e) {
                log.warn("FCM send failed uid={} issueId={}", uid, issueId, e);
            }
        }
        if (delivered == 0) {
            log.warn("All FCM sends failed uid={} issueId={}", uid, issueId);
            return aiDataStore.saveNewsletter(uid, ready.withFailed());
        }

        return aiDataStore.saveNewsletter(uid, ready.withSent(Instant.now()));
    }

    public void sendDue() {
        for (NewsletterDto due : aiDataStore.listDueNewsletters(Instant.now())) {
            try {
                send(due.uid(), due.id());
            } catch (Exception e) {
                log.warn("Due newsletter failed uid={} id={}", due.uid(), due.id(), e);
                try {
                    aiDataStore.saveNewsletter(due.uid(), due.withFailed());
                } catch (Exception persistError) {
                    log.warn("Failed to mark newsletter failed id={}", due.id(), persistError);
                }
            }
        }
    }

    private String resolveKind(String uid, String medicationId) {
        if (medicineInfoUsedToday(uid)) {
            return NewsletterContext.KIND_HEALTH;
        }
        return resolveMedication(uid, medicationId) == null
                ? NewsletterContext.KIND_HEALTH
                : NewsletterContext.KIND_MEDICINE;
    }

    private AiMedicationDto resolveMedication(String uid, String medicationId) {
        if (medicationId != null && !medicationId.isBlank()) {
            AiMedicationDto exact = aiDataStore.getMedication(uid, medicationId).orElse(null);
            if (exact != null) {
                return exact;
            }
        }
        List<AiMedicationDto> medications = aiDataStore.listMedications(uid);
        return medications.isEmpty() ? null : medications.get(0);
    }

    private boolean medicineInfoUsedToday(String uid) {
        Instant start = LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
        Instant end = LocalDate.now(ZONE).plusDays(1).atStartOfDay(ZONE).toInstant();
        return aiDataStore.listNewsletters(uid).stream()
                .filter(item -> NewsletterContext.KIND_MEDICINE.equals(item.kind()))
                .filter(item -> !"failed".equals(item.status()))
                .anyMatch(item -> {
                    Instant at = item.sentAt() != null ? item.sentAt() : item.scheduledAt();
                    return at != null && !at.isBefore(start) && at.isBefore(end);
                });
    }

    private NewsletterContext context(String uid, String medicationId, String trigger, String kind, Instant at) {
        String name = aiDataStore.findUserName(uid).orElse("사용자");
        String drug = "";
        String dosage = "";
        String frequency = "";
        String category = "MEDICATION";
        AiMedicationDto medication = resolveMedication(uid, medicationId);
        if (medication != null) {
            if (medication.name() != null && !medication.name().isBlank()) {
                drug = medication.name();
            }
            if (medication.timing() != null && !medication.timing().isBlank()) {
                frequency = medication.timing();
            }
            if (medication.dosePerDay() != null) {
                frequency = (frequency.isBlank() ? "" : frequency + " · ") + "1일 " + medication.dosePerDay() + "회";
            }
            if (medication.analysis() != null) {
                if (drug.isBlank() && medication.analysis().drugName() != null) {
                    drug = medication.analysis().drugName();
                }
                if (medication.analysis().dosage() != null) {
                    dosage = medication.analysis().dosage();
                }
                if (medication.analysis().frequency() != null && !medication.analysis().frequency().isBlank()) {
                    frequency = medication.analysis().frequency();
                }
            }
        }
        return new NewsletterContext(
                name, drug, dosage, frequency, category, trigger, kind, timeOfDay(at));
    }

    private static String timeOfDay(Instant at) {
        Instant when = at != null ? at : Instant.now();
        int hour = LocalTime.ofInstant(when, ZONE).getHour();
        if (hour < 12) {
            return "morning";
        }
        if (hour < 18) {
            return "afternoon";
        }
        return "evening";
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max - 1) + "...";
    }

    private static boolean hasContent(NewsletterDto newsletter) {
        return newsletter.title() != null && !newsletter.title().isBlank();
    }

    private static String newId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
