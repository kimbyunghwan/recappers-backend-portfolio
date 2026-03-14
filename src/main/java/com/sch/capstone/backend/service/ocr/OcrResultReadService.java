package com.sch.capstone.backend.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OcrResultReadService {

    private final StorageProps storageProps;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ocr.base-url}")
    private String ocrPublicBaseUrl;

    private Path ocrDir(Long uploadId) {
        return Paths.get(storageProps.getLocal().getBaseDir())
                .resolve(storageProps.getPrefix().getExtracted())
                .resolve(String.valueOf(uploadId));
    }

    /** 해당 업로드의 최신 OCR JSON 경로 찾기 */
    public Optional<Path> findLatestJson(Long uploadId) {
        Path dir = ocrDir(uploadId);
        if (!Files.isDirectory(dir)) return Optional.empty();

        try (var s = Files.list(dir)) {
            return s.filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith("ocr-") && name.endsWith(".json");
                    })
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 최신 OCR JSON을 DTO로 읽기 */
    public Optional<OcrResponseDTO> loadLatest(Long uploadId) {
        var opt = findLatestJson(uploadId);
        if (opt.isEmpty()) return Optional.empty();
        try (var in = Files.newInputStream(opt.get())) {
            return Optional.of(mapper.readValue(in, OcrResponseDTO.class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 특정 페이지의 image_url 가져오기 */
    public String getPageImageUrl(Long uploadId, int pageNo) {
        var dto = loadLatest(uploadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.ACCEPTED, "OCR running"));
        return dto.getPages().stream()
                .filter(p -> p.getPage_number() == pageNo)
                .findFirst()
                .map(OcrResponseDTO.Page::getImage_url)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "page not found: " + pageNo));
    }

    public String getPageImageUrlNormalized(Long uploadId, int pageNo) {
        String raw = getPageImageUrl(uploadId, pageNo);
        return normalizeToAbsolute(raw);
    }

    private String normalizeToAbsolute(String u) {
        if (u == null || u.isBlank()) throw new IllegalArgumentException("empty image_url");
        if (u.startsWith("http://") || u.startsWith("https://")) return u; // 이미 절대 URL
        String base = ocrPublicBaseUrl.replaceAll("/+$", "");
        if (u.startsWith("/")) return base + u;
        return base + "/" + u;
    }
}

