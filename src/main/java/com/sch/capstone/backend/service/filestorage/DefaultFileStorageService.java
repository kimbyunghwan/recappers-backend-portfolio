package com.sch.capstone.backend.service.filestorage;

import com.sch.capstone.backend.service.blobstorage.BlobStorage;
import com.sch.capstone.backend.service.blobstorage.BlobStorageKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DefaultFileStorageService implements FileStorageService{

    private final BlobStorage blob;
    private final BlobStorageKeyFactory keyFactory;

    @Override
    public String save(MultipartFile file, Long userId) throws IOException {
        String key = keyFactory.uploadKey(userId, file.getOriginalFilename());
        blob.put(key, file.getBytes(), file.getContentType());
        return key;
    }

    @Override
    public Resource loadAsResource(String storageKey) throws IOException {
        return new InputStreamResource(blob.get(storageKey));
    }
}

