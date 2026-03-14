package com.sch.capstone.backend.dto.extraction;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedTextMetaResponseDTO {
    private Long id;
    private Long uploadId;
    private String storageKey;
    private OffsetDateTime createdAt;
    private Integer charCount;
    private Integer pageCount;
}