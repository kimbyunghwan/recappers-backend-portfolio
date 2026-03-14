package com.sch.capstone.backend.service.blobstorage;

import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface BlobStorage {
    String put(String key, byte[] bytes, @Nullable String contentType) throws IOException;
    InputStream get(String key) throws IOException;
    void delete(String key) throws IOException;
}