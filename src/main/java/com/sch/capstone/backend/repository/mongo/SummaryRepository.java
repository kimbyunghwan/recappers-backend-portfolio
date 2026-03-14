package com.sch.capstone.backend.repository.mongo;

import com.sch.capstone.backend.document.SummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// 요약 및 생성 문항
public interface SummaryRepository extends MongoRepository<SummaryDocument, String> {
    List<SummaryDocument> findBySttResultId(Long sttResultId); // STT 결과 ID에 연결된 요약문 문서 조회
    Optional<SummaryDocument> findTopByUploadIdOrderByCreatedAtDesc(Long uploadId); // 특정 업로드 ID의 최신 요약문 1건 조회
}
