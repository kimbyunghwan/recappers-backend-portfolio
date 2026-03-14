package com.sch.capstone.backend.dto.stt;

import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SttResultResponseDTO {
    private Long id;
    private Long uploadId;
    private String storageKey;
    private OffsetDateTime createdAt;
    private Integer durationSec;
    private Integer charCount;
}
