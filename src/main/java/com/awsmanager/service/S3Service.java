package com.awsmanager.service;

import com.awsmanager.dto.*;
import com.awsmanager.exception.ResourceNotFoundException;
import com.awsmanager.exception.S3OperationException;
import com.awsmanager.model.S3File;
import com.awsmanager.model.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3FileRepository s3FileRepository;

    @Value("${aws.s3.bucket-name}")
    private String defaultBucket;

    @Value("${aws.s3.presigned-url-expiry-minutes:60}")
    private long presignedUrlExpiryMinutes;

    @Value("${app.upload.allowed-extensions:jpg,jpeg,png,gif,pdf,txt,csv,json,zip}")
    private String allowedExtensions;

    // ======================== UPLOAD ========================

    @Transactional
    public S3FileResponse uploadFile(MultipartFile file, String bucketName, Map<String, String> metadata) {
        validateFile(file);
        String bucket = bucketName != null ? bucketName : defaultBucket;
        String objectKey = generateObjectKey(file.getOriginalFilename());

        try {
            // Faz upload para S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .metadata(metadata != null ? metadata : Map.of())
                .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Gera URL do objeto
            String s3Url = String.format("s3://%s/%s", bucket, objectKey);

            // Persiste registro no banco
            S3File s3File = S3File.builder()
                .bucketName(bucket)
                .objectKey(objectKey)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .uploadStatus(S3File.UploadStatus.COMPLETED)
                .s3Url(s3Url)
                .metadata(metadata)
                .build();

            S3File saved = s3FileRepository.save(s3File);
            log.info("Arquivo '{}' enviado para S3: {}/{}", file.getOriginalFilename(), bucket, objectKey);

            return toFileResponse(saved);

        } catch (IOException e) {
            throw new S3OperationException("Erro ao ler arquivo para upload: " + e.getMessage());
        } catch (S3Exception e) {
            throw new S3OperationException("Erro ao enviar para S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== LISTAGEM ========================

    public PagedResponse<S3FileResponse> listFiles(String bucketName, int page, int size) {
        String bucket = bucketName != null ? bucketName : defaultBucket;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("uploadedAt").descending());

        Page<S3File> filesPage = s3FileRepository.findByBucketNameAndUploadStatusNot(
            bucket, S3File.UploadStatus.DELETED, pageRequest
        );

        List<S3FileResponse> content = filesPage.getContent().stream()
            .map(this::toFileResponse)
            .toList();

        return new PagedResponse<>(
            content, page, size,
            filesPage.getTotalElements(),
            filesPage.getTotalPages(),
            filesPage.isLast()
        );
    }

    // ======================== PRESIGNED URL ========================

    public PresignedUrlResponse generatePresignedUrl(String bucketName, String objectKey) {
        String bucket = bucketName != null ? bucketName : defaultBucket;

        // Verifica se objeto existe
        s3FileRepository.findByBucketNameAndObjectKey(bucket, objectKey)
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado: " + objectKey));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
            .getObjectRequest(getObjectRequest)
            .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        return new PresignedUrlResponse(presignedUrl, objectKey, presignedUrlExpiryMinutes);
    }

    // ======================== DELETE ========================

    @Transactional
    public void deleteFile(String bucketName, String objectKey) {
        String bucket = bucketName != null ? bucketName : defaultBucket;

        S3File s3File = s3FileRepository.findByBucketNameAndObjectKey(bucket, objectKey)
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo não encontrado: " + objectKey));

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

            s3Client.deleteObject(deleteRequest);

            s3File.setUploadStatus(S3File.UploadStatus.DELETED);
            s3File.setDeletedAt(OffsetDateTime.now());
            s3FileRepository.save(s3File);

            log.info("Arquivo deletado do S3: {}/{}", bucket, objectKey);

        } catch (S3Exception e) {
            throw new S3OperationException("Erro ao deletar do S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== BUCKET STATS ========================

    public S3BucketStats getBucketStats(String bucketName) {
        String bucket = bucketName != null ? bucketName : defaultBucket;
        long count = s3FileRepository.countByBucketNameAndUploadStatus(bucket, S3File.UploadStatus.COMPLETED);
        Long totalSize = s3FileRepository.sumFileSizeByBucket(bucket);
        long size = totalSize != null ? totalSize : 0L;

        return new S3BucketStats(bucket, count, size, formatFileSize(size));
    }

    // ======================== LISTAGEM DE BUCKETS ========================

    public List<Map<String, String>> listBuckets() {
        try {
            return s3Client.listBuckets().buckets().stream()
                .map(b -> Map.of(
                    "name", b.name(),
                    "creationDate", b.creationDate().toString()
                ))
                .toList();
        } catch (S3Exception e) {
            throw new S3OperationException("Erro ao listar buckets: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== HELPERS ========================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio não permitido.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            List<String> allowed = Arrays.asList(allowedExtensions.split(","));
            if (!allowed.contains(extension)) {
                throw new IllegalArgumentException("Extensão não permitida: " + extension);
            }
        }
    }

    private String generateObjectKey(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String name = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
        return String.format("uploads/%s/%s_%s", timestamp.substring(0, 8), uuid, name);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private S3FileResponse toFileResponse(S3File f) {
        return S3FileResponse.builder()
            .id(f.getId())
            .bucketName(f.getBucketName())
            .objectKey(f.getObjectKey())
            .originalFilename(f.getOriginalFilename())
            .contentType(f.getContentType())
            .fileSizeBytes(f.getFileSizeBytes())
            .fileSizeFormatted(f.getFileSizeBytes() != null ? formatFileSize(f.getFileSizeBytes()) : null)
            .s3Url(f.getS3Url())
            .uploadStatus(f.getUploadStatus() != null ? f.getUploadStatus().name() : null)
            .uploadedAt(f.getUploadedAt())
            .metadata(f.getMetadata())
            .build();
    }
}
