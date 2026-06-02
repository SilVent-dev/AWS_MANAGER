package com.awsmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ec2StatsDto {

    private long running;
    private long stopped;
    private long pending;
    private long terminated;
    private long total;
}
