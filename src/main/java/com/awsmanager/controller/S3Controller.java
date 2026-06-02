package com.awsmanager.controller;

import com.awsmanager.dto.ApiResponse;
import com.awsmanager.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller REST para operações S3.
 *
 * Endpoints:
 *   GET    /api/s3/buckets              - Listar buckets
 *   GET    /api/s3/files                - Listar arquivos (paginado)
 *   POST   /api/s3/files/upload         - Upload de arquivo
 *   GET    /api/s3/files/presigned-url  - Gerar URL pré-assinada
 *   DELETE /api/s3/files/{objectKey}    - Deletar arquivo
 *   GET    /api/s3/stats                - Estatísticas do bucket
 */
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/buckets")
    public ResponseEntity<ApiResponse<?>> listBuckets() {
        var buckets = s3Service.listBuckets();
        return ResponseEntity.ok(ApiResponse.ok(buckets));
    }

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<?>> listFiles(
        @RequestParam(required = false) String bucket,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var result = s3Service.listFiles(bucket, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> uploadFile(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) String bucket,
        @RequestParam(required = false) Map<String, String> metadata
    ) {
        var result = s3Service.uploadFile(file, bucket, metadata);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(result, "Arquivo enviado com sucesso!"));
    }

    @GetMapping("/files/presigned-url")
    public ResponseEntity<ApiResponse<?>> getPresignedUrl(
        @RequestParam String objectKey,
        @RequestParam(required = false) String bucket
    ) {
        var result = s3Service.generatePresignedUrl(bucket, objectKey);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/files")
    public ResponseEntity<ApiResponse<?>> deleteFile(
        @RequestParam String objectKey,
        @RequestParam(required = false) String bucket
    ) {
        s3Service.deleteFile(bucket, objectKey);
        return ResponseEntity.ok(ApiResponse.ok(null, "Arquivo deletado com sucesso!"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getBucketStats(
        @RequestParam(required = false) String bucket
    ) {
        var stats = s3Service.getBucketStats(bucket);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
