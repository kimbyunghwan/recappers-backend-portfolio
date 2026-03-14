package com.sch.capstone.backend.service;

import com.sch.capstone.backend.dto.stt.SttResultRequestDTO;
import com.sch.capstone.backend.entity.SttResult;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.repository.jpa.SttResultRepository;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// STT 결과에 대한 비즈니스 로직을 처리하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class SttResultService {

    // STT 결과를 DB와 연결하기 위한 Repository
    private final SttResultRepository sttResultRepository;

    private final UploadRepository uploadRepository;

    // 특정 업로드 ID에 연결된 STT 결과 목록을 조회하는 메서드
    @Transactional(readOnly = true)
    public List<SttResult> getByUploadId(Long uploadId) {
        return sttResultRepository.findByUploadId(uploadId);
    }

    // STT 메타 데이터 저장하는 메서드
    @Transactional
    public SttResult save(SttResultRequestDTO dto) {
        Upload upload = uploadRepository.findById(dto.getUploadId())
                .orElseThrow(() -> new IllegalArgumentException("Upload not found"));

        SttResult entity = SttResult.builder()
                .upload(upload)
                .storageKey(dto.getStorageKey())
                .durationSec(dto.getDurationSec())
                .charCount(dto.getCharCount())
                .build();

        return sttResultRepository.save(entity);
    }

}
