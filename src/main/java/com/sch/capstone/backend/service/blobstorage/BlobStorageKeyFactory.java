package com.sch.capstone.backend.service.blobstorage;

import com.sch.capstone.backend.config.StorageProps;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlobStorageKeyFactory {
    private final StorageProps props;

    public String extractedKey(Long uploadId) {
        return buildTxt(props.getPrefix().getExtracted(), uploadId);
    }
    public String sttKey(Long uploadId) {
        return buildTxt(props.getPrefix().getStt(), uploadId);
    }

    public String uploadKey(Long userId, @Nullable String originalName) {
        String ext = (originalName != null && originalName.contains(".")) ?
                originalName.substring(originalName.lastIndexOf('.') + 1) : "bin";
        return buildWithExt(props.getPrefix().getUploads(), userId, ext);
    }

    private String buildTxt(String prefix, Long uploadId) {
        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return "%s/%s/%d/%s.txt".formatted(prefix, date, uploadId, UUID.randomUUID());
    }

    private String buildWithExt(String prefix, Long ownerId, String ext) {
        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return "%s/%s/%d/%s.%s".formatted(prefix, date, ownerId, UUID.randomUUID(), ext);
    }
}
