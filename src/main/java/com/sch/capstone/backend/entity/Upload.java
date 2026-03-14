package com.sch.capstone.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sch.capstone.backend.enums.ProcessingStage;
import com.sch.capstone.backend.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "uploads")
public class Upload {

    /** 업로드 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업로드한 사용자
     * - FK 관계 (User 엔티티 참조)
     * - EAGER: 즉시 로딩 방식(업로드 조회 시 작성자 정보도 같이 나오게 하기 위함)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 저장된 파일 이름 */
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    /** 업로드 시간 */
    @Column(name = "upload_time", nullable = false)
    private OffsetDateTime uploadTime;

    /** 업로드 상태 (READY, PROCESSING, DONE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private UploadStatus status;

    /** 처리 상태(OCR_RUNNING, OCR_DONE, OCR_FAILED) **/
    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private ProcessingStage stage;

    /** MIME 타입 (예: application/pdf, audio/wav, image/png) */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /** 파일 크기 (bytes 단위) */
    @Column(name = "size")
    private Long size;

    /** 저장소 키 (실제 파일이 저장된 경로/식별자) */
    @Column(name = "storage_key", length = 500, nullable = false)
    private String storageKey;

    /** INSERT 전에 자동 실행, 업로드 시간 세팅 */
    @PrePersist
    public void prePersist() {
        this.uploadTime = OffsetDateTime.now();

        if (this.status == null) {
            this.status = UploadStatus.QUEUED;
        }
    }

}
