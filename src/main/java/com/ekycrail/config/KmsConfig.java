package com.ekycrail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
public class KmsConfig {

    /**
     * Production bean: Uses AWS default credential chain (IAM roles in ECS/Fargate).
     * Marked as @Lazy to defer initialization until first use.
     */
    @Bean
    @Lazy
    @Primary
    @ConditionalOnProperty(name = "aws.kms.enabled", havingValue = "true", matchIfMissing = true)
    public KmsClient kmsClient(
            @Value("${aws.region}") String awsRegion,
            @Value("${aws.kms.key-arn}") String kmsKeyArn
    ) {
        if (awsRegion == null || awsRegion.isBlank()) {
            throw new IllegalStateException("AWS region must be configured via aws.region");
        }
        if (kmsKeyArn == null || kmsKeyArn.isBlank()) {
            throw new IllegalStateException("KMS key ARN must be configured via aws.kms.key-arn");
        }

        return KmsClient.builder()
                .region(Region.of(awsRegion))
                // Uses the AWS default credential provider chain (IAM roles in ECS/Fargate).
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean(name = "kmsKeyArn")
    public String kmsKeyArn(@Value("${aws.kms.key-arn}") String kmsKeyArn) {
        return kmsKeyArn;
    }
}

