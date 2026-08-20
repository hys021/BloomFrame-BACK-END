package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.AiMedicationDto;
import com.bloomframe.server.ai.dto.NewsletterDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AiDataStore {

    Optional<String> findUserName(String uid);

    Optional<AiMedicationDto> getMedication(String uid, String medicationId);

    List<AiMedicationDto> listMedications(String uid);

    AiMedicationDto updateMedication(String uid, String medicationId, AiMedicationDto medication);

    List<String> listDeviceTokens(String uid);

    NewsletterDto saveNewsletter(String uid, NewsletterDto newsletter);

    Optional<NewsletterDto> getNewsletter(String uid, String issueId);

    List<NewsletterDto> listNewsletters(String uid);

    List<NewsletterDto> listDueNewsletters(Instant now);

    Optional<NewsletterDto> findPendingNewsletterByAlarm(String uid, String alarmId, Instant alarmAt);
}
