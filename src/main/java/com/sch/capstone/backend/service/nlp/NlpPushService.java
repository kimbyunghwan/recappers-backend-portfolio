package com.sch.capstone.backend.service.nlp;

import com.sch.capstone.backend.dto.nlp.OcrPagePayload;
import com.sch.capstone.backend.dto.nlp.OcrToNlpPayload;
import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import com.sch.capstone.backend.service.ocr.OcrResultReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NlpPushService {

    @Qualifier("nlpClient")
    private final WebClient nlpClient;

    private final OcrResultReadService readService;

    final String url = "/ocr/result";

    /** OCR JSON을 페이지 단위 요약용으로 NLP에 전송 */
    public Mono<String> sendOcrPages(Long uploadId, OcrResponseDTO ocr) {

        String sessionId = (ocr.getSession_id() != null && !ocr.getSession_id().isBlank())
                ? ocr.getSession_id()
                : String.valueOf(uploadId);

        var payload = OcrToNlpPayload.builder()
                .filename(ocr.getFilename())
                .totalPages(ocr.getTotal_pages())
                .sessionId(sessionId)
                .allText(ocr.getAll_text() == null ? "" : ocr.getAll_text())
                .pages(
                        ocr.getPages().stream().map(p -> OcrPagePayload.builder()
                                // 보통 1-based 요구 → 0이 들어오면 1로 보정
                                .pageNumber(p.getPage_number() <= 0 ? 1 : p.getPage_number())
                                .text(p.getText() == null ? "" : p.getText())
                                .imageUrl(readService.getPageImageUrlNormalized(uploadId, p.getPage_number()))
                                .build()
                        ).toList()
                )
                .build();

        return nlpClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("NLP HTTP " + resp.statusCode() + " body=" + body)))
                )
                // 서버가 바디를 돌려주면 String, 아니면 toBodilessEntity()로 바꿔도 됨
                .bodyToMono(String.class)
                .doOnSubscribe(s -> log.info("[NLP SEND] POST {} uploadId={} session_id={} filename={}",
                        url, uploadId, sessionId, ocr.getFilename()))
                .doOnSuccess(res -> log.info("[NLP SEND] OK uploadId={} session_id={} resp={}",
                        uploadId, sessionId, res))
                .doOnError(e -> log.error("[NLP SEND] FAIL uploadId={} session_id={} msg={}",
                        uploadId, sessionId, e.getMessage(), e));
    }
}