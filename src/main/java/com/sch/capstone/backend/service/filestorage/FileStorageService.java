package com.sch.capstone.backend.service.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String save(MultipartFile file, Long userId) throws IOException; // 저장 후 storageKey 반환
    Resource loadAsResource(String storageKey) throws IOException;
}
