package com.sch.capstone.backend.service;

import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class NlpSendService {

    private final WebClient nlpClient;
    private final StorageProps props;
    private final UploadRepository uploadRepo;

    @Value("${app.backend.public-base-url}")
    private String backendBaseUrl;

    @Async("uploadTaskExecutor")
    public void sendByMultipart(Long uploadId) throws IOException {
        Upload up = uploadRepo.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        // 1) 파일 경로 확인 로그
        String prefix = props.getPrefix().getUploads().replace('\\','/');
        String key    = up.getStorageKey().replace('\\','/');
        Path rel = key.startsWith(prefix + "/") ? Paths.get(key) : Paths.get(prefix, key);
        Path abs = Paths.get(props.getLocal().getBaseDir()).resolve(rel).normalize().toAbsolutePath();
        if (!Files.exists(abs)) throw new FileNotFoundException(abs.toString());
        System.out.println("[NLP] file=" + abs);

        // 2) MIME
        String filename = up.getFileName();
        String mime = Files.probeContentType(abs);
        if (mime == null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".pdf")) mime = "application/pdf";
            else if (lower.endsWith(".wav")) mime = "audio/wav";
            else if (lower.endsWith(".mp3")) mime = "audio/mpeg";
            else mime = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // 3) 멀티파트 구성 (중복 제거)
        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("file", new FileSystemResource(abs.toFile()))
                .filename(filename)
                .contentType(MediaType.parseMediaType(mime));

        mb.part("summary_max_tokens", "400");
        mb.part("num_questions", "5");
        mb.part("fusion_strategy", "late");

        String callback = backendBaseUrl + "/api/nlp/callback/result?uploadId=" + uploadId;
        System.out.println("[NLP] Using callback=" + callback);
        mb.part("callback_url", callback);

        System.out.println("[NLP] POST /test/full-pipeline callback=" + callback);

        // 5) 타임아웃 + 에러바디 로깅
        var resp = nlpClient.post()
                .uri("/stt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(mb.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, cr ->
                        cr.bodyToMono(String.class)
                                .map(body -> new RuntimeException("NLP error " + cr.statusCode() + " body=" + body)))
                .toBodilessEntity()
                .doOnSubscribe(s -> System.out.println("-> NLP POST /test/full-pipeline"))
                .doOnSuccess(res -> System.out.println("[NLP] sent. status=" + res.getStatusCode()))
                .doOnError(ex -> System.err.println("[NLP] send failed: " + ex.getMessage()))
                .subscribe();
    }
}

