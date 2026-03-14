package com.sch.capstone.backend.service;

import com.sch.capstone.backend.dto.extraction.ExtractedTextMetaRequestDTO;
import com.sch.capstone.backend.entity.ExtractedTextMeta;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.repository.jpa.ExtractedTextMetaRepository;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractedTextMetaService {

    private final ExtractedTextMetaRepository extractedTextMetaRepository;
    private final UploadRepository uploadRepository;

    @Transactional(readOnly = true)
    public List<ExtractedTextMeta> getByUploadId(Long uploadId) {
        return extractedTextMetaRepository.findByUploadId(uploadId);
    }

    @Transactional
    public ExtractedTextMeta save(ExtractedTextMetaRequestDTO dto) {
        Upload upload = uploadRepository.findById(dto.getUploadId())
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + dto.getUploadId()));

        ExtractedTextMeta entity = ExtractedTextMeta.builder()
                .upload(upload)
                .storageKey(dto.getStorageKey())
                .charCount(dto.getCharCount())
                .pageCount(dto.getPageCount())
                .build();

        return extractedTextMetaRepository.save(entity);
    }
}
