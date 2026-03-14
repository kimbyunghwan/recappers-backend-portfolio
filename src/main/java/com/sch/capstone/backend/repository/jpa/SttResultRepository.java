package com.sch.capstone.backend.repository.jpa;

import com.sch.capstone.backend.entity.SttResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// STT 관련 메타데이터 저장
public interface SttResultRepository extends JpaRepository<SttResult, Long> {
    List<SttResult> findByUploadId(Long uploadId);

    List<SttResult> findByUploadIdOrderByCreatedAtAsc(Long uploadId);
}
