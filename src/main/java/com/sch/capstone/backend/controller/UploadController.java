package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.processing.UploadProcessor;
import com.sch.capstone.backend.dto.upload.UploadResponseDTO;
import com.sch.capstone.backend.dto.user.UserDTO;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.entity.User;
import com.sch.capstone.backend.repository.jpa.UserRepository;
import com.sch.capstone.backend.service.UploadService;
import com.sch.capstone.backend.service.filestorage.DefaultFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

// 업로드 관련 컨트롤러
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;
    private final UserRepository userRepository;
    private final DefaultFileStorageService defaultFileStorageService;

    private final UploadProcessor processor;

    @Operation(summary = "내 업로드 목록 조회")
    @GetMapping("/me")
    public ResponseEntity<List<UploadResponseDTO>> myUploads(Authentication auth) {
        Long myId = currentUserId(auth);
        List<UploadResponseDTO> dtos = uploadService.getUploadByUserId(myId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "업로드 ID로 단건 조회(본인 소유것만)")
    @GetMapping("/{id}")
    public ResponseEntity<UploadResponseDTO> getMineById(
            @Parameter(description = "업로드 ID") @PathVariable Long id,
            Authentication auth) {

        String email = auth.getName(); // JwtAuthenticationFilter에서 principal=email
        Upload u = uploadService.getByIdAndUserEmail(id, email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Not found"));

        return ResponseEntity.ok(toDto(u));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "업로드 파일 다운로드(본인 소유만)")
    public ResponseEntity<Resource> downloadById(
            @PathVariable Long id,
            org.springframework.security.core.Authentication auth
    ) throws java.io.IOException {

        String email = auth.getName();
        Upload u = uploadService.getByIdAndUserEmailOrThrow(id, email);

        // storageKey를 이용해 실제 파일 로드
        Resource file = defaultFileStorageService.loadAsResource(u.getStorageKey());

        return ResponseEntity.ok()
                // Content-Type
                .contentType(MediaType.parseMediaType(
                        java.util.Optional.ofNullable(u.getMimeType())
                                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                // 다운로드 처리
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment() // 파일로 내려받는 용도
                                .filename(u.getFileName(), java.nio.charset.StandardCharsets.UTF_8)
                                .build().toString())
                .body(file);
    }

    @Operation(summary = "파일 업로드(원본 저장 + 메타 저장)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadFile(
            @RequestPart("file") MultipartFile file,
            Authentication auth) throws IOException {

        // 1) 로그인 사용자 이메일 → User 조회
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) 파일 저장 + 메타 저장
        Upload saved = uploadService.uploadAndSave(file, user);

        // 3) 비동기 처리 트리거 (요약/전사 파이프라인 시작)
        processor.process(saved.getId());

        // 4) 응답
        URI location = URI.create("/api/uploads/" + saved.getId());
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @Operation(summary = "전체 업로드 목록 조회(관리자 전용)")
    @GetMapping
    public ResponseEntity<List<UploadResponseDTO>> findAll() {
        List<Upload> uploads = uploadService.findAll();
        List<UploadResponseDTO> dtos = uploads.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "전체 업로드 삭제 (관리자 전용/테스트용)")
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        uploadService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 업로드 전체 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyUploads(Authentication auth) {
        Long myId = currentUserId(auth);
        uploadService.deleteAllByUserId(myId);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    private UploadResponseDTO toDto(Upload upload) {
        String apiDownloadUrl = "/api/uploads/" + upload.getId() + "/download";
        return UploadResponseDTO.builder()
                .id(upload.getId())
                .fileName(upload.getFileName())
                .uploadTime(upload.getUploadTime() != null ? upload.getUploadTime().toString() : null)
                .status(upload.getStatus() != null ? upload.getStatus().name() : null)
                .downloadUrl(apiDownloadUrl)
                .user(new UserDTO(
                        upload.getUser().getId(),
                        upload.getUser().getName(),
                        upload.getUser().getEmail(),
                        upload.getUser().getRole()
                ))
                .build();
    }
}
