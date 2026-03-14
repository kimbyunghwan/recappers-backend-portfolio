package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.service.ocr.OcrPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadOcrController {

    private final OcrPipelineService ocrPipelineService;

    @PostMapping("/{uploadId}/ocr")
    public ResponseEntity<?> runOcr(@PathVariable Long uploadId) {
        ocrPipelineService.runOcrByUploadId(uploadId);
        return ResponseEntity.ok(Map.of("uploadId", uploadId, "status", "OCR_DONE"));
    }

}
