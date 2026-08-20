package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.MedicinePhotoAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NoopAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(NoopAiClient.class);

    @Override
    public String provider() {
        return "noop";
    }

    @Override
    public MedicinePhotoAnalysisResult analyzeMedicinePhoto(
            byte[] image, String contentType, List<String> healthConditions) {
        log.info("[noop] analyzeMedicinePhoto bytes={} type={} conditions={}",
                image.length, contentType, healthConditions);
        List<MedicineAnalysisDto> medications = List.of(
                new MedicineAnalysisDto(
                        "타이레놀",
                        "1정",
                        "1일 3회",
                        3,
                        "식후",
                        List.of("08:00", "12:00", "20:00"),
                        "mock OCR — set GEMINI_API_KEY to analyze real photos",
                        0.2),
                new MedicineAnalysisDto(
                        "오메프라졸",
                        "1캡슐",
                        "1일 1회",
                        1,
                        "식전",
                        List.of("08:00"),
                        "mock second drug from pill bag",
                        0.2));
        return new MedicinePhotoAnalysisResult(
                medications,
                "mock pill bag — " + medications.size() + " drugs",
                0.2);
    }

    @Override
    public NewsletterContent generateNewsletter(NewsletterContext context) {
        log.info("[noop] generateNewsletter kind={} drug={}", context.kind(), context.drugName());
        if (NewsletterContext.KIND_HEALTH.equals(context.kind())) {
            return new NewsletterContent(
                    "오늘 알아두면 좋은 팁",
                    "물을 조금씩 자주 마시고, 한 시간마다 가볍게 몸을 움직여 주세요. 오래 앉아 있으면 다리가 붓기 쉽습니다.",
                    List.of("식후에는 천천히 걸어 보세요.", "짠 음식은 혈압에 부담이 될 수 있습니다.", "잠들기 전 스마트폰은 조금 멀리 두세요.")
            );
        }
        String drug = context.drugName() == null || context.drugName().isBlank() ? "약" : context.drugName();
        return new NewsletterContent(
                "복용 후 주의점",
                drug + "을(를) 드신 뒤에는 속이 불편하거나 어지러울 수 있습니다. 눕거나 천천히 움직이고, 술·운전은 피하는 것이 안전합니다. 평소와 다른 증상이 있으면 약국이나 병원에 문의하세요.",
                List.of("충분한 물과 함께 복용하세요.", "졸음이 오면 쉬고 운전은 미루세요.", "증상이 심하면 임의로 용량을 바꾸지 말고 전문가에게 확인하세요.")
        );
    }
}
