package com.ekycrail.integration;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class NagarikAppClient {

    private static final int ERROR_MESSAGE_LIMIT = 256;

    private final WebClient webClient;
    private final String verifyPath;
    private final int responseTimeoutMs;

    public NagarikAppClient(
            WebClient.Builder webClientBuilder,
            @Value("${nagarik.base-url}") String baseUrl,
            @Value("${nagarik.verify-path:/v1/identity/verify}") String verifyPath,
            @Value("${nagarik.connect-timeout-ms:1500}") int connectTimeoutMs,
            @Value("${nagarik.response-timeout-ms:2500}") int responseTimeoutMs
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS)));

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.verifyPath = verifyPath;
        this.responseTimeoutMs = responseTimeoutMs;
    }

    public Mono<NagarikDispatchResult> dispatchVerification(IdentityVerificationEnvelope envelope) {
        UpstreamVerificationRequest outboundRequest = UpstreamVerificationRequest.fromEnvelope(envelope);

        return webClient.post()
                .uri(verifyPath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(outboundRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("upstream_http_error")
                        .map(this::toUpstreamException))
                .bodyToMono(UpstreamVerificationResponse.class)
                .timeout(Duration.ofMillis(responseTimeoutMs))
                .map(this::toDispatchResult)
                // Parse and transport failures are normalized to non-PII fallback metadata.
                .onErrorResume(TimeoutException.class, ex -> Mono.just(NagarikDispatchResult.timeoutFallback()))
                .onErrorResume(WebClientRequestException.class, ex -> Mono.just(NagarikDispatchResult.networkFallback()))
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(NagarikDispatchResult.httpFallback(ex.getStatusCode().value())))
                .onErrorResume(DecodingException.class, ex -> Mono.just(NagarikDispatchResult.parseFallback()))
                .onErrorReturn(NagarikDispatchResult.genericFallback());
    }

    private NagarikDispatchResult toDispatchResult(UpstreamVerificationResponse response) {
        return new NagarikDispatchResult(
                response.status,
                response.resultCode,
                response.signedJwt,
                response.keyId,
                response.expiresAt,
                false
        );
    }

    private UpstreamNagarikException toUpstreamException(String rawMessage) {
        String sanitized = rawMessage == null ? "upstream_http_error" : rawMessage;
        if (sanitized.length() > ERROR_MESSAGE_LIMIT) {
            sanitized = sanitized.substring(0, ERROR_MESSAGE_LIMIT);
        }
        return new UpstreamNagarikException(sanitized);
    }

    private static final class UpstreamNagarikException extends RuntimeException {
        private UpstreamNagarikException(String message) {
            super(message);
        }
    }

    public record NagarikDispatchResult(
            String status,
            String resultCode,
            String signedJwt,
            String keyId,
            Long expiresAt,
            boolean fallback
    ) {
        public static NagarikDispatchResult timeoutFallback() {
            return new NagarikDispatchResult("FAILED", "UPSTREAM_TIMEOUT", null, null, null, true);
        }

        public static NagarikDispatchResult networkFallback() {
            return new NagarikDispatchResult("FAILED", "UPSTREAM_NETWORK_ERROR", null, null, null, true);
        }

        public static NagarikDispatchResult httpFallback(int statusCode) {
            return new NagarikDispatchResult("FAILED", "UPSTREAM_HTTP_" + statusCode, null, null, null, true);
        }

        public static NagarikDispatchResult parseFallback() {
            return new NagarikDispatchResult("FAILED", "UPSTREAM_PARSE_ERROR", null, null, null, true);
        }

        public static NagarikDispatchResult genericFallback() {
            return new NagarikDispatchResult("FAILED", "UPSTREAM_CLIENT_ERROR", null, null, null, true);
        }
    }

    private record UpstreamVerificationRequest(
            String transactionId,
            String bankId,
            List<String> scopes,
            String purposeCode,
            String consentReference,
            String nonce,
            Instant requestTimestamp
    ) {
        private static UpstreamVerificationRequest fromEnvelope(IdentityVerificationEnvelope envelope) {
            List<String> scopeValues = envelope.getVerificationRequest().getRequestedScopes().stream()
                    .map(scope -> scope == null ? null : scope.toString())
                    .toList();

            return new UpstreamVerificationRequest(
                    stringValue(envelope.getRequestContext().getTransactionId()),
                    stringValue(envelope.getRequestContext().getBankId()),
                    scopeValues,
                    envelope.getVerificationRequest().getPurposeCode().toString(),
                    stringValue(envelope.getVerificationRequest().getConsentReference()),
                    stringValue(envelope.getRequestContext().getRequestNonce()),
                    envelope.getRequestContext().getRequestTimestamp()
            );
        }
    }

    private record UpstreamVerificationResponse(
            String status,
            String resultCode,
            String signedJwt,
            String keyId,
            Long expiresAt,
            Instant respondedAt
    ) {
    }

    private static String stringValue(CharSequence value) {
        return value == null ? null : value.toString();
    }
}


