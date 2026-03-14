package com.sch.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "extracted_text_meta")
public class ExtractedTextMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 업로드 파일의 텍스트인지 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    private Upload upload;

    /** S3 등 외부 스토리지에 저장된 텍스트 파일 경로 */
    @Column(name = "storage_key", length = 512)
    private String storageKey;

    /** 추출 완료 시각 (DB에서 자동 기록) */
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private OffsetDateTime createdAt;

    /** 추출된 텍스트 전체 길이(문자 수) */
    @Column(name = "char_count")
    private Integer charCount;

    /** 원본 PDF 페이지 수 */
    @Column(name = "page_count")
    private Integer pageCount;

}
