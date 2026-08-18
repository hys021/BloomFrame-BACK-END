package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.MedicineAnalysisDto;

public interface AiClient {

    String provider();

    MedicineAnalysisDto analyzeMedicinePhoto(byte[] image, String contentType);

    NewsletterContent generateNewsletter(NewsletterContext context);
}
