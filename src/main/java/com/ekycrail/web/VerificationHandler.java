package com.ekycrail.web;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.service.VerificationService;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class VerificationHandler {

    private final VerificationService verificationService;

    public VerificationHandler(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    public Mono<ServerResponse> verify(ServerRequest request) {
        return request.bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new InvalidPayloadException("Request body must not be empty")))
                .flatMap(this::parseAndValidateEnvelope)
                .flatMap(verificationService::verify)
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result))
                .onErrorResume(InvalidPayloadException.class, ex -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new ApiError("INVALID_REQUEST", ex.getMessage())));
    }

    private Mono<IdentityVerificationEnvelope> parseAndValidateEnvelope(String payload) {
        return Mono.fromCallable(() -> {
                    SpecificDatumReader<IdentityVerificationEnvelope> reader =
                            new SpecificDatumReader<>(IdentityVerificationEnvelope.class);
                    IdentityVerificationEnvelope envelope = reader.read(
                            null,
                            DecoderFactory.get().jsonDecoder(IdentityVerificationEnvelope.getClassSchema(), payload)
                    );

                    if (envelope == null
                            || envelope.getRequestContext() == null
                            || envelope.getVerificationRequest() == null
                            || envelope.getAuditMetadata() == null) {
                        throw new InvalidPayloadException("Payload is missing required verification sections");
                    }
                    return envelope;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(ex -> ex instanceof InvalidPayloadException
                        ? ex
                        : new InvalidPayloadException("Payload does not conform to IdentityVerificationEnvelope schema"));
    }

    public record ApiError(String code, String message) {
    }

    private static final class InvalidPayloadException extends RuntimeException {
        private InvalidPayloadException(String message) {
            super(message);
        }
    }
}

