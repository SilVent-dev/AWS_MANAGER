package com.awsmanager.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface S3FileRepository extends JpaRepository<S3File, UUID> {
    Page<S3File> findByBucketNameAndUploadStatusNot(
        String bucketName,
        S3File.UploadStatus status,
        Pageable pageable
    );

    Optional<S3File> findByBucketNameAndObjectKey(String bucketName, String objectKey);

    @Query("SELECT SUM(f.fileSizeBytes) FROM S3File f WHERE f.bucketName = :bucket AND f.uploadStatus = 'COMPLETED'")
    Long sumFileSizeByBucket(@Param("bucket") String bucketName);

    long countByBucketNameAndUploadStatus(String bucketName, S3File.UploadStatus status);
}
