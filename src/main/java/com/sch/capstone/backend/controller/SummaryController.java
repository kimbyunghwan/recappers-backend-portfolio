package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.document.SummaryDocument;
import com.sch.capstone.backend.dto.summary.SummaryRequestDTO;
import com.sch.capstone.backend.dto.summary.SummaryResponseDTO;
import com.sch.capstone.backend.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 요약 관련 컨트롤러
@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(summary = "STT 결과 ID로 요약 조회")
    @GetMapping("/stt-result/{sttResultId}")
    public ResponseEntity<List<SummaryResponseDTO>> getBySttResultId(
            @Parameter(description = "STT 결과 ID") @PathVariable Long sttResultId) {

        List<SummaryDocument> summaries = summaryService.getSummariesBySttResultId(sttResultId);
        List<SummaryResponseDTO> dtoList = summaries.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "요약 저장")
    @PostMapping
    public ResponseEntity<Void> save(
            @Parameter(description = "저장할 요약 정보") @RequestBody SummaryRequestDTO dto) {
        summaryService.saveSummary(fromDto(dto));
        return ResponseEntity.ok().build();
    }

    private SummaryResponseDTO toDto(SummaryDocument doc) {
        return new SummaryResponseDTO(
                doc.getId(),
                doc.getSttResultId(),
                doc.getSummaryText(),
                doc.getModelUsed(),
                doc.getQuestions() == null ? List.of() : doc.getQuestions());
    }

    private SummaryDocument fromDto(SummaryRequestDTO dto) {
        SummaryDocument doc = new SummaryDocument();
        doc.setSttResultId(dto.getSttResultId());
        doc.setSummaryText(dto.getSummaryText());
        doc.setModelUsed(dto.getModelUsed());
        doc.setQuestions(dto.getQuestions());
        return doc;
    }
}