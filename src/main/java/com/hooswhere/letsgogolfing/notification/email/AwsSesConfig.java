package com.hooswhere.letsgogolfing.notification.email;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@EnableConfigurationProperties(AwsSesProps.class)
public class AwsSesConfig {
    @Bean
    public SesV2Client sesV2Client(AwsSesProps awsSesProps) {
        return SesV2Client.builder()
                .region(Region.of(awsSesProps.region()))
                .credentialsProvider(getCredentialsProvider(awsSesProps))
                .build();
    }

    private AwsCredentialsProvider getCredentialsProvider(AwsSesProps awsSesProps) {
        // If access key and secret key are provided, use them
        if (awsSesProps.accessKey() != null && !awsSesProps.accessKey().isEmpty() &&
            awsSesProps.secretKey() != null && !awsSesProps.secretKey().isEmpty()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsSesProps.accessKey(), awsSesProps.secretKey())
            );
        }
        // Otherwise, use the default credentials provider chain
        // (environment variables, IAM roles, etc.)
        return DefaultCredentialsProvider.create();
    }
}

