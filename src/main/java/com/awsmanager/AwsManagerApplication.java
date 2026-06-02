package com.awsmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AwsManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsManagerApplication.class, args);
    }
}
