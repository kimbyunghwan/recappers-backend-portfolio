package com.sch.capstone.backend.service.ocr;

import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.enums.ProcessingStage;
import com.sch.capstone.backend.enums.UploadStatus;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import com.sch.capstone.backend.service.nlp.NlpPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrPipelineService {

    private final UploadRepository uploadRepository;
    private final StorageProps storageProps; // app.storage.local.base-dir, prefix.uploads 등
    private final OcrClientService ocrClientService; // OCR 서버 호출
    private final OcrIngestService ocrIngestService; // 결과 저장(DB+파일)
    private final NlpPushService nlpPushService;

    @Transactional
    public void runOcrByUploadId(Long uploadId){
        // 업로드 조회
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "upload not found: " + uploadId));

        // 저장된 PDF 실제 경로 계산
        Path pdfPath = resolvePdfPath(uploadId, upload);

        log.info("OCR input path: {}", pdfPath.toAbsolutePath());

        boolean exists = Files.exists(pdfPath);
        long size = -1L;

        if (exists) {
            try {
                size = Files.size(pdfPath);
            } catch (IOException e) {
                log.warn("파일 크기 조회 실패: {}", pdfPath, e);
            }
        }

        log.info("exists={} size={}", exists, size);

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "stored pdf not found: " + pdfPath);
        }

        // 3) 상태/단계 갱신
        upload.setStatus(UploadStatus.PROCESSING);
        upload.setStage(ProcessingStage.OCR_RUNNING);

        // 4) OCR 서버에 파일로 업로드 (multipart/form-data)
        FileSystemResource pdfResource = new FileSystemResource(pdfPath.toFile());
        OcrResponseDTO resp = ocrClientService.extract(pdfResource);

        // 5) 결과 저장 (텍스트 파일 저장 + extracted_text_meta 기록 + DONE/OCR_DONE)
        ocrIngestService.save(uploadId, resp);

        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        // fire-and-forget 비동기 전송
                        nlpPushService.sendOcrPages(uploadId, resp)
                                .doOnSubscribe(s -> log.info("[OCR→NLP] start uploadId={}", uploadId))
                                .doOnSuccess(res -> log.info("[OCR→NLP] ok uploadId={} resp={}", uploadId, res))
                                .doOnError(e -> log.error("[OCR→NLP] fail uploadId=" + uploadId, e))
                                .subscribe();
                    }
                });
    }

    /** 저장된 PDF 경로를 계산: storageKey가 있으면 그걸, 없으면 규칙대로 조합 */
    private Path resolvePdfPath(Long uploadId, Upload upload) {
        String baseDir = storageProps.getLocal().getBaseDir();         // ./capstone-storage
        String uploadsPrefix = storageProps.getPrefix().getUploads();  // files/uploads

        if (upload.getStorageKey() != null && !upload.getStorageKey().isBlank()) {
            return Paths.get(baseDir, upload.getStorageKey());
        }
        return Paths.get(baseDir, uploadsPrefix, String.valueOf(uploadId), upload.getFileName());
    }
}
