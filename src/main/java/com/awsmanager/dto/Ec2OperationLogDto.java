package com.awsmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ec2OperationLogDto {

    private UUID id;
    private String instanceId;
    private String operation;
    private String status;
    private String errorMessage;
    private OffsetDateTime executedAt;
}
