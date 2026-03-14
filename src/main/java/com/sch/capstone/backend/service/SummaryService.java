package com.sch.capstone.backend.service;

import com.sch.capstone.backend.document.SummaryDocument;
import com.sch.capstone.backend.repository.mongo.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// MongoDB에 저장된 Summary 데이터를 처리하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class SummaryService {

    // MongoDB의 summaries 컬렉션과 연결된 Repository
    private final SummaryRepository summaryRepository;

    // stt 결과 id와 연결된 모든 요약문 리스트를 반환
    public List<SummaryDocument> getSummariesBySttResultId(Long sttResultId) {
        return summaryRepository.findBySttResultId(sttResultId);
    }

    // 요약문 저장
    public void saveSummary(SummaryDocument summary) {
        summaryRepository.save(summary);
    }
}
