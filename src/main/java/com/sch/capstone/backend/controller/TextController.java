package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.service.TextPersistenceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 원본 텍스트에 관한, 경로/메타데이터를 관리하는 컨트롤러
@RestController
@RequestMapping("/api/texts")
@RequiredArgsConstructor
public class TextController {

    private final TextPersistenceService textService;

    // 추출된 텍스트를 저장하는 API
    @PostMapping("/extracted")
    public ResponseEntity<?> saveExtracted(@RequestBody ExtractedTextSaveRequest req) {
        var saved = textService.storeExtractedText(req.getUploadId(), req.getText(), req.getPageCount());
        
        // 201 Created + 저장 결과 일부 메타데이터 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "storageKey", saved.getStorageKey(),
                "charCount", saved.getCharCount(),
                "pageCount", saved.getPageCount()
        ));
    }

    // STT 텍스트를 저장하는 API
    @PostMapping("/stt")
    public ResponseEntity<?> saveStt(@RequestBody SttTextSaveRequest req) {
        var saved = textService.storeSttText(req.getUploadId(), req.getText(), req.getDurationSec());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "storageKey", saved.getStorageKey(),
                "charCount", saved.getCharCount(),
                "durationSec", saved.getDurationSec()
        ));
    }
}

@Data
class ExtractedTextSaveRequest {
    Long uploadId; // 업로드 ID
    String text;  // 텍스트 내용
    Integer pageCount; // 페이지 수
}

@Data
class SttTextSaveRequest {
    Long uploadId; // 업로드 ID
    String text; // 텍스트 내용
    Integer durationSec; // 음성 길이
}