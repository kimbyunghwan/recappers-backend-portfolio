package com.sch.capstone.backend.service.blobstorage;

import com.sch.capstone.backend.config.StorageProps;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalBlobStorage implements BlobStorage {

    private final StorageProps props;

    private Path root() {
        return Path.of(props.getLocal().getBaseDir()).toAbsolutePath().normalize();
    }

    @Override
    public String put(String key, byte[] bytes, String contentType) throws IOException {
        Path path = root().resolve(key).normalize();
        Files.createDirectories(path.getParent());
        Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
        return key;
    }

    @Override
    public InputStream get(String key) throws IOException {
        return Files.newInputStream(root().resolve(key).normalize(), StandardOpenOption.READ);
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(root().resolve(key).normalize());
    }
}
