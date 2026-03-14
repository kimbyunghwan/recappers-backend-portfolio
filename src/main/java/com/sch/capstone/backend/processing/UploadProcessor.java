package com.sch.capstone.backend.processing;

import com.sch.capstone.backend.enums.UploadStatus;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import com.sch.capstone.backend.service.filestorage.DefaultFileStorageService;
import com.sch.capstone.backend.service.nlp.PdfService;
import com.sch.capstone.backend.service.nlp.SttService;
import com.sch.capstone.backend.service.nlp.SummarizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 파일 불러오기, STT, PDF처리, 요약, 상태 업데이트
@Service
@RequiredArgsConstructor
public class UploadProcessor {
    private final UploadRepository uploadRepo;
    private final DefaultFileStorageService defaultFileStorageService;
    private final SttService stt;
    private final PdfService pdf;
    private final SummarizerService sum;

    public void process(Long uploadId) {
        doProcess(uploadId);
    }
    
    @Transactional
    void doProcess(Long uploadId) {
        // 업로드 엔티티 조회 & 상태 = PROCESSING으로 변경
        var u = uploadRepo.findById(uploadId).orElseThrow();
        u.setStatus(UploadStatus.PROCESSING);
        uploadRepo.save(u);

        try {
            // 저장소에서 파일 업로드
            var res = defaultFileStorageService.loadAsResource(u.getStorageKey());
            try (var in = res.getInputStream()) {

                // MIME 타입 확인하고 오디오 파일이면 STT, 아니면 PDF 텍스트 추출
                boolean isAudio = u.getMimeType() != null && u.getMimeType().startsWith("audio/");

                String text = isAudio
                        ? stt.transcribe(in, u.getMimeType())
                        : pdf.extractText(in);

                // 요약 실행
                String summary = sum.summarize(text);

                // 성공적으로 끝나면 상대 "DONE"
                u.setStatus(UploadStatus.DONE);
            }
        } catch (Exception e) {
            // 처리 도중 예외 발생하면 상태 "FAILED"
            u.setStatus(UploadStatus.FAILED);
        }

        uploadRepo.save(u);
    }
}
