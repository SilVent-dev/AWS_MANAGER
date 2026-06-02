package com.awsmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class S3FileResponse {

    private UUID id;
    private String bucketName;
    private String objectKey;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private String fileSizeFormatted;
    private String s3Url;
    private String uploadStatus;
    private OffsetDateTime uploadedAt;
    private Map<String, String> metadata;
}
