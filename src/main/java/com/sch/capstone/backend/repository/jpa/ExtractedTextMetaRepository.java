package com.sch.capstone.backend.repository.jpa;

import com.sch.capstone.backend.entity.ExtractedTextMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// PDF -> 텍스트 (메타데이터 DB)
public interface ExtractedTextMetaRepository extends JpaRepository<ExtractedTextMeta, Long> {
    List<ExtractedTextMeta> findByUploadId(Long uploadId); // 특정 업로드 ID에 대한 추출 텍스트 메타데이터 목록 조회
}
