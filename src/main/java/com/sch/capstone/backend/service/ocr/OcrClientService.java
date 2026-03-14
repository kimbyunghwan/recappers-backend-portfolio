package com.sch.capstone.backend.service.ocr;

import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OcrClientService {

    private final WebClient webClient;

    public OcrClientService(@Qualifier("ocrClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${ocr.server.url}")
    private String ocrUrl;

    @Value("${ocr.server.token:}")
    private String token;

    public OcrResponseDTO extract(FileSystemResource pdf) {

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", pdf);

        log.info("➡OCR call {} (file={})", ocrUrl, pdf.getFilename());

        return webClient.post()
                .uri(ocrUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(h -> {
                    if (token != null && !token.isBlank()) {
                        h.setBearerAuth(token);
                    }
                })
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), r ->
                        r.bodyToMono(String.class).flatMap(msg ->
                                Mono.<Throwable>error(new ResponseStatusException(
                                        r.statusCode(), "OCR 4xx: " + msg))))
                .onStatus(status -> status.is5xxServerError(), r ->
                        r.bodyToMono(String.class).flatMap(msg ->
                                Mono.<Throwable>error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "OCR 5xx: " + msg))))
                .bodyToMono(OcrResponseDTO.class)
                .block();
    }
}