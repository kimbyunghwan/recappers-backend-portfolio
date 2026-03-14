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
@Table(name = "stt_result_meta")
public class SttResult {

    /** STT결과 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 파일의 STT 결과인지 (FK: uploads.id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    private Upload upload;

    /** 전사 텍스트가 저장된 S3 경로(파일 → 텍스트) */
    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    /** 전사 완료 시각 (DB가 자동 기록) */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private OffsetDateTime createdAt;

    /** 원본 오디오 길이(초) */
    @Column(name = "duration_sec")
    private Integer durationSec;

    /** 전사 텍스트 전체 길이(문자 수) */
    @Column(name = "char_count")
    private Integer charCount;
}
