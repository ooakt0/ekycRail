package com.ekycrail.security;

public interface RequestSignatureValidator {
    boolean isValid(String apiKey, String signature, byte[] canonicalRequest);
}
