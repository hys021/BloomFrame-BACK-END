package com.bloomframe.server.controller;

import com.bloomframe.server.ai.MedicineAnalyzeService;
import com.bloomframe.server.ai.dto.MedicineAnalyzeResponse;
import com.bloomframe.server.common.security.AuthenticatedUid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medications/{id}")
public class MedicineAnalyzeController {

    private final MedicineAnalyzeService medicineAnalyzeService;

    public MedicineAnalyzeController(MedicineAnalyzeService medicineAnalyzeService) {
        this.medicineAnalyzeService = medicineAnalyzeService;
    }

    @PostMapping("/analyze")
    public MedicineAnalyzeResponse analyze(
            @PathVariable String id,
            @AuthenticatedUid String uid,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return medicineAnalyzeService.analyze(uid, id, file);
    }
}
