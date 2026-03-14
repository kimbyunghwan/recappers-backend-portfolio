package com.sch.capstone.backend.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NlpStageCallbackPayload(
        @JsonProperty("job_id") String jobId,
        String status,
        String stage,
        Integer sequence,

        @JsonProperty("is_final") Boolean isFinal,
        SttPart stt,
        PdfPart pdf,
        SummaryPart summary,
        QuestionsPart questions,
        Metrics metrics
) {
    public boolean finalStage() { return Boolean.TRUE.equals(isFinal); }

    public record SttPart(String text, String summary) {}
    public record PdfPart(Integer chars, String summary) {}
    public record SummaryPart(String model, String text) {}
    public record QuestionsPart(List<QuestionItem> questions) {}
    public record QuestionItem(
            @JsonProperty("q") String q,
            @JsonProperty("a") String a,
            String type,
            List<String> options,
            String source
    ) {}
    public record Metrics(@JsonProperty("latency_ms") Long latencyMs) {}
}