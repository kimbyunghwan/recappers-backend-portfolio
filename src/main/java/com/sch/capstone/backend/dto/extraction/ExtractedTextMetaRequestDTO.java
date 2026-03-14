package com.sch.capstone.backend.dto.extraction;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExtractedTextMetaRequestDTO {
    /** 어떤 업로드의 텍스트인지 */
    private Long uploadId;
    /** S3 저장 키 (예: uploads/{uploadId}/extracted.txt) */
    private String storageKey;
    /** 텍스트 전체 길이(문자 수) */
    private Integer charCount;
    /** 원본 PDF 페이지 수 */
    private Integer pageCount;
}
