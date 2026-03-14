package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.dto.extraction.ExtractedTextMetaRequestDTO;
import com.sch.capstone.backend.dto.stt.SttResultRequestDTO;
import com.sch.capstone.backend.service.ExtractedTextMetaService;
import com.sch.capstone.backend.service.SttResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

// 파일 업로드 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/texts")
public class TextIngestController {

    // 파일 저장 관련
    private final StorageProps props;
    // 추출 텍스트 메타데이터 저장 서비스
    private final ExtractedTextMetaService extractedTextMetaService;
    // STT 결과 저장 서비스
    private final SttResultService sttResultService;

    // 추출된 텍스트 파일 업로드
    @PostMapping(
            value = "/extracted/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadExtracted(
            @RequestParam Long uploadId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required=false) Integer pageCount
    ) throws IOException {
        // 스토리지에 파일 저장하고 key 생성
        String key = saveTxtToStorage(props.getPrefix().getExtracted(), uploadId, file);

        // 저장된 파일의 전체 글자 수
        int charCount = Files.readString(resolve(key)).length();

        // 메타데이터 DB에 저장(업로드 ID, 저장 경로, 글자수, 페이지수)
        var saved = extractedTextMetaService.save(new ExtractedTextMetaRequestDTO(uploadId, key, charCount, pageCount)); // DTO 필드명에 맞춰 수정

        // 응답 반환(storageKey, metaId)
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "storageKey", key,
                "metaId", saved.getId()
        ));
    }

    // STT 텍스트 파일 업로드
    @PostMapping(
            value = "/stt/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadStt(
            @RequestParam Long uploadId,
            @RequestPart("file") MultipartFile file, // 업로드 되는 STT 파일
            @RequestParam Integer durationSec // 음성 파일 길이
    ) throws IOException {
        // 스토리지에 파일 저장하고 키 생성
        String key = saveTxtToStorage(props.getPrefix().getStt(), uploadId, file);
        
        // 저장된 파일의 전체 글자 수 계산
        int charCount = Files.readString(resolve(key)).length();

        // STT 결과 DB에 저장
        var saved = sttResultService.save(new SttResultRequestDTO(
                uploadId, key, durationSec, charCount)); // DTO 필드명에 맞춰 수정

        // 응답 반환(storageKey, sttResultId)
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "storageKey", key,
                "sttResultId", saved.getId()
        ));
    }

    // 업로드된 MultipartFile(txt)을 로컬스토리지에 저장
    private String saveTxtToStorage(String prefix, Long uploadId, MultipartFile file) throws IOException {
        // 저장될 파일명 생성(u{uploadId}_타임스탬프.txt)
        String fname = "u" + uploadId + "_" + System.currentTimeMillis() + ".txt";// u는 upload를 의미
        String key = prefix + "/" + fname;
        
        // 스토리지 키(문자열) -> 실제 OS파일 경로로 변환
        Path path = resolve(key);
        // 파일이 저장될 상위 디렉터리 생성(없으면 새로 생성, 있으면 무시됨)
        Files.createDirectories(path.getParent());

        // 파일 저장(기존 파일 있으면 덮어쓴다.)
        try (var in = file.getInputStream()) {
            Files.copy(in, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return key;
    }

    // 스토리지 key를 실제 로컬 경로로 변환
    private Path resolve(String storageKey) {
        return Paths.get(props.getLocal().getBaseDir()) // 스토리지 루트 경로 생성
                .toAbsolutePath().normalize() // 절대 경로화 + 정규화
                .resolve(storageKey).normalize(); // storageKey를 붙여서 최종 경로 생성 + 정규화
    }
}
