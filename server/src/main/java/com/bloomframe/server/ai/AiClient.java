package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.MedicinePhotoAnalysisResult;

import java.util.List;

public interface AiClient {

    String provider();

    MedicinePhotoAnalysisResult analyzeMedicinePhoto(byte[] image, String contentType, List<String> healthConditions);

    NewsletterContent generateNewsletter(NewsletterContext context);
}
