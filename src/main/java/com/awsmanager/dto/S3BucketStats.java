package com.awsmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3BucketStats {

    private String bucketName;
    private long totalFiles;
    private Long totalSizeBytes;
    private String totalSizeFormatted;
}
