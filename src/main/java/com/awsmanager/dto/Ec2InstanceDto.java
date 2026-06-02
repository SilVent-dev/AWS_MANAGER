package com.awsmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ec2InstanceDto {

    private String instanceId;
    private String instanceType;
    private String state;
    private String stateName;
    private String publicIpAddress;
    private String privateIpAddress;
    private String publicDnsName;
    private String launchTime;
    private String platform;
    private String architecture;
    private List<String> securityGroups;
    private Map<String, String> tags;
    private String availabilityZone;
    private String imageId;
}
