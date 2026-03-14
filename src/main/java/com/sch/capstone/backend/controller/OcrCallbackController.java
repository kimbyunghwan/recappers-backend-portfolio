package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import com.sch.capstone.backend.service.nlp.NlpPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr/callback")
public class OcrCallbackController {

    private final NlpPushService nlpPushService;

    /**
     * OCR 서버 콜백을 받아서 즉시 NLP 서버로 포워딩.
     */
    @PostMapping("/result")
    public ResponseEntity<Void> receive(
            @RequestParam Long uploadId,
            @RequestBody OcrResponseDTO dto) {

        if (uploadId == null || dto == null || dto.getPages() == null || dto.getPages().isEmpty()) {
            log.warn("[OCR→NLP] invalid payload: uploadId={}, dto.pages={}",
                    uploadId, (dto == null ? null : dto.getPages()));
            return ResponseEntity.badRequest().build();
        }

        log.info("[OCR→NLP] forwarding start uploadId={}, pages={}", uploadId, dto.getPages().size());

        // 비동기 전송 (fire-and-forget)
        nlpPushService.sendOcrPages(uploadId, dto)
                .doOnSuccess(resp -> log.info("[OCR→NLP] success uploadId={}, resp={}", uploadId, resp))
                .doOnError(err -> log.error("[OCR→NLP] failed uploadId=" + uploadId, err))
                .subscribe();

        return ResponseEntity.accepted().build();
    }
}