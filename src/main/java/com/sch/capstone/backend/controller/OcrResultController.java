package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.service.ocr.OcrResultReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class OcrResultController {

    private final OcrResultReadService readService;
    @Qualifier("ocrClient") private final WebClient ocrClient;

    /** 결과 조회: 없으면 202(RUNNING), 있으면 200(JSON 그대로) */
    /** 결과 조회: 없으면 202(RUNNING), 있으면 200(JSON 그대로) */
    @GetMapping("/{uploadId}/ocr")
    public ResponseEntity<?> getOcr(@PathVariable Long uploadId) {
        var dto = readService.loadLatest(uploadId);
        if (dto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("status", "RUNNING", "progress", 0));
        }
        return ResponseEntity.ok(dto.get());
    }

//    /** 페이지 이미지: JSON의 image_url로 302 리다이렉트 */
//    @GetMapping("/{uploadId}/ocr/pages/{pageNo}/image")
//    public ResponseEntity<Void> pageImage(@PathVariable Long uploadId, @PathVariable int pageNo) {
//        String url = readService.getPageImageUrlNormalized(uploadId, pageNo);
//        return ResponseEntity.status(302).header("Location", url).build();
//    }

    @GetMapping("/{uploadId}/ocr/pages/{pageNo}/image")
    public ResponseEntity<byte[]> pageImage(@PathVariable Long uploadId, @PathVariable int pageNo) {
        // 1) JSON에서 image_url 뽑고 절대 URL로 정규화
        String url = readService.getPageImageUrlNormalized(uploadId, pageNo);

        // 2) OCR 서버에서 이미지 가져와서 그대로 바이트로 반환 (동일 오리진이므로 CORS 문제 없음)
        var entity = ocrClient.get()
                .uri(url)
                .retrieve()
                .toEntity(byte[].class)
                .block();

        if (entity == null || entity.getBody() == null) {
            return ResponseEntity.status(502).build(); // upstream 문제
        }

        // 3) 원본 Content-Type/Length/Cache-Control 최대한 보존
        var headers = entity.getHeaders();
        return ResponseEntity
                .status(200)
                .headers(h -> {
                    h.setContentType(headers.getContentType() != null
                            ? headers.getContentType()
                            : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                    if (headers.getContentLength() >= 0) h.setContentLength(headers.getContentLength());
                    var cc = headers.getCacheControl();
                    if (cc != null) h.setCacheControl(cc);
                })
                .body(entity.getBody());
    }
}