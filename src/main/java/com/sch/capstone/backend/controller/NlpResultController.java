package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.nlp.NlpResultDTO;
import com.sch.capstone.backend.service.nlp.NlpResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// NLP 처리 결과를 제공하는 REST 컨트롤러
@RestController
@RequestMapping("/api/nlp/results")
@RequiredArgsConstructor
public class NlpResultController {

    private final NlpResultService nlpResultService;

    @GetMapping("/{uploadId}")
    public NlpResultDTO getResult(@PathVariable Long uploadId) {
        return nlpResultService.findByUploadId(uploadId);
    }
}
