package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.nlp.NlpStageCallbackPayload;
import com.sch.capstone.backend.service.nlp.NlpResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/nlp/callback")
@RequiredArgsConstructor
public class NlpCallbackController {

    private final NlpResultService nlpResultService;

//    @Value("${app.nlp.callback.api-key:}")
//    private String apiKey;

    @PostMapping("/result")
    public ResponseEntity<Void> receive(
            // 헤더에 담겨오는 보안키
            //@RequestHeader(name = "X-API-KEY", required = false) String key,
            @RequestParam(name = "uploadId") Long uploadId,
            // NLP 서버가 보낸 콜백 데이터
            @RequestBody NlpStageCallbackPayload payload) {

        // 콜백 진입 로그
        log.info("NLP callback IN uploadId={}, payloadClass={}",
                uploadId, (payload == null ? null : payload.getClass().getSimpleName()));
        log.debug("payload={}", payload);

        if (uploadId == null) {
            log.warn("NLP callback missing uploadId");
            throw new IllegalArgumentException("uploadId is required (use callback_url?...uploadId=)");
        }

        nlpResultService.applyStage(uploadId, payload);

        log.info("NLP callback DONE uploadId={}", uploadId);
        return ResponseEntity.ok().build();
    }
}