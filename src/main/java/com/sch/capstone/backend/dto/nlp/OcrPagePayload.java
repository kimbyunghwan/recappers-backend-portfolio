package com.sch.capstone.backend.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrPagePayload {
    @JsonProperty("page_number")
    private int pageNumber;

    private String text;

    @JsonProperty("image_url")
    private String imageUrl;
}