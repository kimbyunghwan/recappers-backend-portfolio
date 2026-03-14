package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.stt.SttResultRequestDTO;
import com.sch.capstone.backend.dto.stt.SttResultResponseDTO;
import com.sch.capstone.backend.entity.SttResult;
import com.sch.capstone.backend.service.SttResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// STT 결과 메타데이터를 조회/저장하는 컨트롤러
@RestController
@RequestMapping("/api/stt-result-meta")
@RequiredArgsConstructor
public class SttResultController {

    private final SttResultService sttResultService;

    @Operation(summary = "업로드 ID로 메타 조회")
    @GetMapping("/upload/{uploadId}")
    public ResponseEntity<List<SttResultResponseDTO>> getByUploadId(
            @Parameter(description = "업로드 ID") @PathVariable Long uploadId) {

        List<SttResult> sttResults = sttResultService.getByUploadId(uploadId);
        List<SttResultResponseDTO> dtos = sttResults.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "STT 메타 저장")
    @PostMapping
    public ResponseEntity<SttResultResponseDTO> save(@RequestBody SttResultRequestDTO dto) {
        SttResult saved = sttResultService.save(dto);
        return ResponseEntity.ok(toDto(saved));
    }

    private SttResultResponseDTO toDto(SttResult sttResult) {
        return SttResultResponseDTO.builder()
                .id(sttResult.getId())
                .uploadId(sttResult.getUpload().getId())
                .storageKey(sttResult.getStorageKey())
                .createdAt(sttResult.getCreatedAt())
                .durationSec(sttResult.getDurationSec())
                .charCount(sttResult.getCharCount())
                .build();
    }
}
