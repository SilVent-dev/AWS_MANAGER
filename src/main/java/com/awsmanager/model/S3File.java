package com.awsmanager.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "s3_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "upload_status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UploadStatus uploadStatus = UploadStatus.COMPLETED;

    @Column(name = "s3_url", columnDefinition = "TEXT")
    private String s3Url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, String> metadata;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = OffsetDateTime.now();
    }

    public enum UploadStatus {
        PENDING, COMPLETED, FAILED, DELETED
    }
}
