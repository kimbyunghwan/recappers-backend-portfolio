package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.extraction.ExtractedTextMetaRequestDTO;
import com.sch.capstone.backend.dto.extraction.ExtractedTextMetaResponseDTO;
import com.sch.capstone.backend.entity.ExtractedTextMeta;
import com.sch.capstone.backend.service.ExtractedTextMetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 추출된 텍스트 메타데이터 관련 REST 컨트롤러
@RestController
@RequestMapping("/api/extracted-text-meta")
@RequiredArgsConstructor
public class ExtractedTextMetaController {

    private final ExtractedTextMetaService extractedTextMetaService;

    @Operation(summary = "업로드 ID로 추출 텍스트 메타 조회")
    @GetMapping("/upload/{uploadId}")
    public ResponseEntity<List<ExtractedTextMetaResponseDTO>> getByUploadId(
            @Parameter(description = "업로드 ID") @PathVariable Long uploadId) {

        // uploadId로 extracted_text_meta 엔티티들의 내용을 리스트 형태로 가져오기
        List<ExtractedTextMeta> list = extractedTextMetaService.getByUploadId(uploadId);

        // 엔티티들을 ExtractedTextMetaResponseDTO로 변환
        List<ExtractedTextMetaResponseDTO> dtos = list.stream()
                .map(this::toDto)
                .toList();

        // 200 OK + DTO 리스트 반환
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "추출 텍스트 메타 저장")
    @PostMapping
    public ResponseEntity<ExtractedTextMetaResponseDTO> save(
            @RequestBody ExtractedTextMetaRequestDTO dto) {

        ExtractedTextMeta saved = extractedTextMetaService.save(dto);
        
        // 저장된 엔티티들을 DTO로 변환 후 봔한
        return ResponseEntity.ok(toDto(saved));
    }

    // 엔티티 -> ResponseDTO 변환하는 메서드
    private ExtractedTextMetaResponseDTO toDto(ExtractedTextMeta e) {
        return ExtractedTextMetaResponseDTO.builder()
                .id(e.getId())
                .uploadId(e.getUpload().getId())
                .storageKey(e.getStorageKey())
                .createdAt(e.getCreatedAt())
                .charCount(e.getCharCount())
                .pageCount(e.getPageCount())
                .build();
    }
}
