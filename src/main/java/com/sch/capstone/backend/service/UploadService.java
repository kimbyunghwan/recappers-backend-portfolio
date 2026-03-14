package com.sch.capstone.backend.service;

import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.entity.User;
import com.sch.capstone.backend.enums.UploadStatus;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import com.sch.capstone.backend.service.filestorage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;
import java.nio.file.Paths;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

// 업로드 관련 비즈니스 로직을 처리하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadRepository uploadRepository;
    private final FileStorageService fileStorageService;

    // 특정 사용자의 업로드 목록을 조회
    public List<Upload> getUploadByUserId(Long userId) {
        return uploadRepository.findByUserId(userId);
    }

    // 파일 업로드 후 저장
    @Transactional
    public Upload uploadAndSave(MultipartFile file, User me) throws IOException {
        // 원본 파일 이름 정재
        String display = sanitizeOriginal(file.getOriginalFilename());
        // 스토리지 저장 후 storageKey 반환
        String storageKey = fileStorageService.save(file, me.getId());

        Upload upload = new Upload();
        upload.setUser(me);
        upload.setFileName(display);
        upload.setMimeType(safeMime(detectMime(file))); // 파일 형식 검출
        upload.setSize(file.getSize());
        upload.setStorageKey(storageKey);
        upload.setStatus(UploadStatus.QUEUED); // 초기 상태 : 대기 중

        return uploadRepository.save(upload);
    }

    // 원본 파일 정재하는 메서드
    private String sanitizeOriginal(String name) {
        String n = (name == null || name.isBlank()) ? "unnamed" : name;

        n = StringUtils.cleanPath(n).replaceAll("\\p{Cntrl}", ""); // 경로 제거 및 제어 문자 제거
        n = Paths.get(n).getFileName().toString(); // 파일 이름만 추출
        n = n.replaceAll("^[\"'“”‘’]+|[\"'“”‘’]+$", ""); // 앞뒤 따옴표 제거
        n = n.trim().replaceAll("\\s+", " "); // 앞뒤 공백 제거 및 여러 공백을 하나로 축소

        if (n.isBlank()) n = "unnamed"; // 결과가 비어있으면 "unnamed"로 대체

        return (n.length() > 255) ? n.substring(0, 255) : n; // 파일 이름이 길면 255자까지
    }

    // 파일 형식 검출하는 메서드
    private String detectMime(MultipartFile file) {
        // Tika로 내용 기반(확장자가 잘못되어 있어도 실제 바이트를 보고 판별) MIME 감지
        try (InputStream in = file.getInputStream()) {
            String byContent = new Tika().detect(in, file.getOriginalFilename());

            // 내용 기반 결과가 유효하면 반환
            if (byContent != null && !byContent.isBlank()) return byContent;
        } catch (IOException ignore) {}


//        // 내용 기반 실패 시 확장자를 보고 MIME 매핑
//        return MediaTypeFactory
//                .getMediaType(file.getOriginalFilename()) // 확장자만 보고 MIME 타입 추론
//                .map(MediaType::toString) // MediaType 객체가 있을 때만, 문자열로 바꾼다.
//                .orElse(file.getContentType());

        return MediaTypeFactory
                .getMediaType(file.getOriginalFilename())
                .map(MediaType::toString)
                .orElseGet(() -> {
                    String contentType = file.getContentType();
                    return (contentType != null && !contentType.isBlank()) // 빈 문자열이 아니면 그대로 사용
                    ? contentType
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE; // 빈 문자열이면 "알 수 없는 바이너리 데이터"
        });
    }

    // 값이 null이거나 빈 문자열일 때 처리하는 메서드
    private String safeMime(String mime) {
        // 하나라도 참이면 application/octet-stream, 둘 다 거짓이면 그대로 반환
        return (mime == null || mime.isBlank()) ? "application/octet-stream" : mime;
    }

//    // 업로드 ID를 통해 단일 업로드 정보를 조회
//    public Optional<Upload> getById(Long id) {
//        return uploadRepository.findById(id);
//    }

    // DB에 저장된 모든 업로드를 조회
    public List<Upload> findAll() {
        return uploadRepository.findAll();
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        uploadRepository.deleteAllByUserId(userId);
    }

    // 데이터베이스에 저장된 모든 업로드 기록 삭제 (관리자/테스트용)
    @Transactional
    public void deleteAll() {
        uploadRepository.deleteAll();
    }

    public Optional<Upload> getByIdAndUserEmail(Long id, String email) {
        return uploadRepository.findByIdAndUserEmail(id, email);
    }

    public Upload getByIdAndUserEmailOrThrow(Long id, String email) {
        return uploadRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
    }

}
