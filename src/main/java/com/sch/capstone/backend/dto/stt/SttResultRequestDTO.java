package com.sch.capstone.backend.dto.stt;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SttResultRequestDTO {
    /** 어떤 업로드의 STT 결과인지 */
    private Long uploadId;
    /** S3에 저장된 전사 텍스트 경로 */
    private String storageKey;
    /** 원본 오디오 길이(초) */
    private Integer durationSec;
    /** 전사 텍스트 전체 길이(문자 수) */
    private Integer charCount;
}