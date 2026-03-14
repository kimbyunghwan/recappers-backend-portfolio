package com.sch.capstone.backend.repository.jpa;

import com.sch.capstone.backend.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 업로드 파일 메타데이터
public interface UploadRepository extends JpaRepository<Upload, Long> {
    List<Upload> findByUserId(Long userId); // 사용자가 선택한 파일의 uploadId 기준으로 파일 조회
    void deleteAllByUserId(Long userId); // 사용자의 userId가 업로드한 파일 목록 조회
    Optional<Upload> findByIdAndUserEmail(Long id, String email); // 업로드 ID와 사용자 이메일을 동시에 조회(권한 검증 등에 사용)
}
