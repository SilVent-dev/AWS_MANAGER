package com.awsmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${aws.ec2.endpoint}")
    private String ec2Endpoint;

    @Value("${app.env}")
    private String appEnv;

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        );
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider());

        // Em desenvolvimento usa LocalStack, em produção usa AWS real
        if ("development".equals(appEnv)) {
            log.info("S3 configurado para LocalStack: {}", s3Endpoint);
            builder.endpointOverride(URI.create(s3Endpoint))
                   .forcePathStyle(true); // Necessário para LocalStack
        } else {
            log.info("S3 configurado para AWS: região {}", region);
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider());

        if ("development".equals(appEnv)) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }

        return builder.build();
    }

    @Bean
    public Ec2Client ec2Client() {
        var builder = Ec2Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider());

        if ("development".equals(appEnv)) {
            log.info("EC2 configurado para LocalStack: {}", ec2Endpoint);
            builder.endpointOverride(URI.create(ec2Endpoint));
        } else {
            log.info("EC2 configurado para AWS: região {}", region);
        }

        return builder.build();
    }
}
