package com.mini3.backend.global.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AtsS3StorageService {

    private static final Pattern SAFE_NAME = Pattern.compile("[^a-zA-Z0-9._-]");

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 지원 이력서 원본 파일을 S3에 업로드하고 객체 키를 반환한다.
     */
    public String uploadApplicantResume(MultipartFile file, Long applicantId) throws IOException {
        String original = file.getOriginalFilename();
        String safe = sanitizeFileName(original != null ? original : "resume");
        String key = String.format("ats/resumes/%d/%s_%s", applicantId, UUID.randomUUID(), safe);

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        long size = file.getSize();
        if (size >= 0) {
            try (var input = file.getInputStream()) {
                s3Client.putObject(request, RequestBody.fromInputStream(input, size));
            }
        } else {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        }

        return key;
    }

    private static String sanitizeFileName(String name) {
        String base = name.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        base = SAFE_NAME.matcher(base).replaceAll("_");
        if (base.isBlank()) {
            base = "file";
        }
        if (base.length() > 200) {
            base = base.substring(0, 200);
        }
        return base.toLowerCase(Locale.ROOT);
    }
}
