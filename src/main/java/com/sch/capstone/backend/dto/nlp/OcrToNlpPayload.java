package com.sch.capstone.backend.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class OcrToNlpPayload {
    private String filename;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("all_text")
    private String allText;

    private List<OcrPagePayload> pages;
}