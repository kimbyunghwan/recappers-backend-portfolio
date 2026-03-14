package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.service.NlpSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

// NLP 서버로 데이터 전송하는 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nlp")
public class NlpSendController {

    private final NlpSendService svc;

    // 멀티파트 기반 파이프라인
    // 업로드된 파일 자체를 multipart/form-data로 NLP 서버에 전달
    @PostMapping("/send/multipart/{uploadId}")
    public ResponseEntity<Void> sendMultipart(@PathVariable Long uploadId) throws IOException {
        svc.sendByMultipart(uploadId);

        // 요청 받으면 202 응답, NLP 처리 결과는 콜백으로 별도 전달
        return ResponseEntity.accepted().build();
    }
}
