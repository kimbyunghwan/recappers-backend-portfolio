package com.sch.capstone.backend.service;

import com.sch.capstone.backend.service.blobstorage.BlobStorage;
import com.sch.capstone.backend.dto.extraction.ExtractedTextMetaRequestDTO;
import com.sch.capstone.backend.dto.stt.SttResultRequestDTO;
import com.sch.capstone.backend.entity.ExtractedTextMeta;
import com.sch.capstone.backend.entity.SttResult;
import com.sch.capstone.backend.service.blobstorage.BlobStorageKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TextPersistenceService {

    private final BlobStorage blobStorage;
    private final BlobStorageKeyFactory keyFactory;
    private final ExtractedTextMetaService extractedMetaService;
    private final SttResultService sttResultService;

    @Transactional
    public ExtractedTextMeta storeExtractedText(Long uploadId, String text, Integer pageCount) {
        String key = keyFactory.extractedKey(uploadId);
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try {
            blobStorage.put(key, bytes, "text/plain; charset=UTF-8");

            var dto = ExtractedTextMetaRequestDTO.builder()
                    .uploadId(uploadId)
                    .storageKey(key)
                    .charCount(text.codePointCount(0, text.length()))
                    .pageCount(pageCount)
                    .build();

            return extractedMetaService.save(dto);
        } catch (RuntimeException e) {
            try {blobStorage.delete(key); } catch (Exception ignore) {}
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("extracted 텍스트 저장 실패", e);
        }
    }

    @Transactional
    public SttResult storeSttText(Long uploadId, String text, Integer durationSec) {
        String key = keyFactory.sttKey(uploadId);
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try {
            blobStorage.put(key, bytes, "text/plain; charset=UTF-8");

            var dto = SttResultRequestDTO.builder()
                    .uploadId(uploadId)
                    .storageKey(key)
                    .charCount(text.codePointCount(0, text.length()))
                    .durationSec(durationSec)
                    .build();

            return sttResultService.save(dto);
        } catch (RuntimeException e) {
            try {
                blobStorage.delete(key);
            } catch (Exception ignore) {

            }
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("stt 텍스트 저장 실패", e);
        }
    }
}
