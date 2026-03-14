package com.sch.capstone.backend.dto.ocr;

import lombok.Data;

import java.util.List;

@Data
public class OcrResponseDTO {
    private String filename;
    private int total_pages;
    private String session_id;
    private String all_text;
    private List<Page> pages;

    @Data
    public static class Page {
        private int page_number;
        private String text;
        private String image_url;
    }
}