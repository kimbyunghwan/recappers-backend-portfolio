package com.sch.capstone.backend.service.nlp;

import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.dto.nlp.NlpResultDTO;
import com.sch.capstone.backend.dto.question.QuestionViewDTO;
import com.sch.capstone.backend.dto.nlp.NlpStageCallbackPayload;
import com.sch.capstone.backend.document.SummaryDocument;
import com.sch.capstone.backend.dto.question.QuestionDTO;
import com.sch.capstone.backend.entity.SttResult;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.enums.QuestionType;
import com.sch.capstone.backend.enums.UploadStatus;
import com.sch.capstone.backend.repository.jpa.SttResultRepository;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import com.sch.capstone.backend.repository.mongo.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NlpResultService {

    private final UploadRepository uploadRepository;
    private final SttResultRepository sttResultRepository;
    private final SummaryRepository summaryRepository; // Mongo
    private final StorageProps props;

    // NLP 서버에서 보내준 콜백 결과를 받아서 저장
    @Transactional
    public void applyStage(Long uploadId, NlpStageCallbackPayload p) {
        // 업로드 엔티티 조회
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        log.info("[NLP CALLBACK] jobId={}, stage={}, seq={}, final={}",
                p.jobId(), p.stage(), p.sequence(), p.isFinal());

        // STT 엔티티 가져오거나 없으면 생성
        SttResult stt = sttResultRepository.findByUploadId(uploadId)
                .stream().findFirst()
                .orElseGet(() -> {
                    SttResult s = new SttResult();
                    s.setUpload(upload);
                    return s;
                });

        // 단계별 처리
        switch (p.stage() == null ? "" : p.stage()) {
            case "stt_completed" -> handleSttCompleted(stt, p);
            case "summary_completed" -> { // 요약만 갱신
                // stt는 있을 수도/없을 수도 있음 → 요약만 처리
            }
            case "questions_completed" -> { // 최종 단계
                // 요약, 문항 저장
            }
            default -> {
                // 알 수 없는 단계는 로그만
                log.warn("Unknown stage: {}", p.stage());
            }
        }

        // summaries 문서 업서트(없으면 생성, 있으면 부분 업데이트)
        SummaryDocument doc = summaryRepository.findBySttResultId(stt.getId()).stream().findFirst()
                .orElseGet(() -> SummaryDocument.builder()
                        .sttResultId(stt.getId())
                        .uploadId(uploadId)
                        .createdAt(Instant.now())
                        .build());

        // 요약(최종 합성 요약은 summary.text)
        if (p.summary() != null && p.summary().text() != null) {
            doc.setSummaryText(p.summary().text());
            doc.setModelUsed(p.summary().model());
        }

        // 질문 변환/저장 (questions.questions 배열)
        int qCount = (p.questions()!=null && p.questions().questions()!=null)
                ? p.questions().questions().size() : 0;
        log.info("[CHECK] uploadId={} stage={} questionsBlockNull={} count={}",
                uploadId, p.stage(), (p.questions()==null), qCount);

        if (qCount == 0) {
            log.warn("[SKIP] questions empty -> no save (uploadId={})", uploadId);
        } else {
            List<QuestionDTO> list = p.questions().questions().stream().map(q -> {
                QuestionType qt;
                try { qt = QuestionType.from(q.type()); }
                catch (Exception e) {
                    log.warn("[MAP] unknown question type='{}' -> fallback=SUBJECTIVE (uploadId={})",
                            q.type(), uploadId);
                    qt = QuestionType.SUBJECTIVE;
                }
                return QuestionDTO.builder()
                        .questionType(qt)
                        .questionText(q.q())
                        .answer(q.a())
                        .build();
            }).toList();
            doc.setQuestions(list);
            log.info("[SAVE OK] questions uploadId={} count={}", uploadId, list.size());
        }

        summaryRepository.save(doc);

        // 단계 상태/최종 처리
        if ("questions_completed".equalsIgnoreCase(p.stage()) || Boolean.TRUE.equals(p.isFinal())) {
            upload.setStatus(UploadStatus.DONE);
        } else {
            upload.setStatus(UploadStatus.PROCESSING);
        }
        uploadRepository.save(upload);
    }

    @Transactional(readOnly = true)
    public NlpResultDTO findByUploadId(Long uploadId) {
        // uploadId 기준으로 STT 결과 엔티티들을 시간순으로 조회, 각 결과에서 getStorageKey만 추출해서 리스트로 생성
        List<String> stt = sttResultRepository
                .findByUploadIdOrderByCreatedAtAsc(uploadId).stream()
                .map(SttResult::getStorageKey)
                .toList();

        // Summary + Questions 조회(MongoDB)
        var summaryDocOpt = summaryRepository.findTopByUploadIdOrderByCreatedAtDesc(uploadId);
        String summary = summaryDocOpt.map(SummaryDocument::getSummaryText).orElse(null);

        // 저장된 질문 꺼내기
        List<QuestionDTO> stored = summaryDocOpt
                .map(SummaryDocument::getQuestions)
                .orElse(List.of());
        
        // 질문 텍스트만 추출
        List<String> questionsOnly = stored.stream()
                .map(QuestionDTO::getQuestionText)
                .toList();

        // 질문 전체 뷰 객체 생성
        List<QuestionViewDTO> qa = stored.stream()
                .map(q -> QuestionViewDTO.builder()
                        .questionText(q.getQuestionText())
                        .answer(q.getAnswer())
                        .questionType(q.getQuestionType())
                        .options(q.getOptions())
                        .build())
                .toList();

        // 현재 처리 중인 단계
        String stage = stt.isEmpty() ? "PENDING" // STT 결과가 없으면 "PENDING"
                : (summary == null ? "STT_ONLY" // 요약이 없을 때 "STT_ONLY"
                : (questionsOnly.isEmpty() ? "SUMMARY_DONE" : "QA_DONE")); // 질문까지 있으면 "QA_DONE"

        // 최종 DTO 반환
        return NlpResultDTO.builder()
                .uploadId(uploadId)
                .stage(stage)
                .stt(stt)
                .summary(summary)
                .questions(questionsOnly)
                .qa(qa)
                .build();
    }

    // NLP 서버에서 전달된 STT 텍스트를 파일로 저장하고, STT 결과 엔티티에 저장 경로와 글자 수를 반영한 후 DB에 저장
    private void handleSttCompleted(SttResult stt, NlpStageCallbackPayload p) {
        // 1) 콜백 데이터에 STT 텍스트가 있을 때만 처리
        if (p.stt() != null && p.stt().text() != null && !p.stt().text().isBlank()) {
            // 2) 텍스트를 파일로 저장하고 storageKey(파일 경로)를 반환
            String key = persistTextIfNeeded(p.stt().text());
            
            // 3) STT 엔티티에 저장 경로와 글자 수 저장
            stt.setStorageKey(key);
            stt.setCharCount(p.stt().text().length());
        }
        // DB에 저장
        sttResultRepository.save(stt);
    }

    // NLP 서버에서 전달된 STT 텍스트를 파일로 저장해주는 메서드
    private String persistTextIfNeeded(String text) {
        // 저장 경로 prefix
        String prefix = props.getPrefix().getStt().replace('\\','/');

        // 현재 시각 기준 유니크한 파일명 생성
        String fileName = "stt-" + Instant.now().toEpochMilli() + ".txt";
        String key = prefix + "/" + fileName;

        // 절대 경로 계산(basedir + key)
        Path abs = Paths.get(props.getLocal().getBaseDir())
                .resolve(key).normalize().toAbsolutePath();
        try {
            // 상위 디렉토리 생성
            Files.createDirectories(abs.getParent());

            // UTF-8 인코딩으로 텍스트 파일 쓰기
            Files.writeString(abs, text, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, // 없으면 새로 생성
                    StandardOpenOption.TRUNCATE_EXISTING); // 있으면 덮어쓰기
        } catch (IOException e) {
            // 파일 쓰기 실패시 런타임 예외
            throw new RuntimeException("Failed to save STT text file: " + abs, e);
        }
        // 저장된 파일의 key 반환
        return key;
    }
}