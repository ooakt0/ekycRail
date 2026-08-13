package com.ekycrail.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.kms.KmsClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KmsConfigTest {

    @Test
    void shouldCreateKmsClientUsingProvidedRegionAndKeyArn() {
        KmsConfig config = new KmsConfig();

        KmsClient kmsClient = config.kmsClient("ap-south-1", "arn:aws:kms:ap-south-1:123456789012:key/test-key");
        try {
            assertNotNull(kmsClient);
        } finally {
            kmsClient.close();
        }
    }

    @Test
    void shouldFailWhenRegionIsMissing() {
        KmsConfig config = new KmsConfig();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> config.kmsClient(" ", "arn:aws:kms:ap-south-1:123456789012:key/test-key")
        );

        assertEquals("AWS region must be configured via aws.region", ex.getMessage());
    }

    @Test
    void shouldFailWhenKeyArnIsMissing() {
        KmsConfig config = new KmsConfig();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> config.kmsClient("ap-south-1", "")
        );

        assertEquals("KMS key ARN must be configured via aws.kms.key-arn", ex.getMessage());
    }

    @Test
    void shouldExposeKmsKeyArnBeanValue() {
        KmsConfig config = new KmsConfig();

        String keyArn = config.kmsKeyArn("arn:aws:kms:ap-south-1:123456789012:key/test-key");

        assertEquals("arn:aws:kms:ap-south-1:123456789012:key/test-key", keyArn);
    }
}

