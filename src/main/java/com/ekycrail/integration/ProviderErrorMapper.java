package com.ekycrail.integration;

import com.ekycrail.enums.ResultCode;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class ProviderErrorMapper {

    public ResultCode map(Throwable error) {
        if (hasCause(error, TimeoutException.class)) {
            return ResultCode.UPSTREAM_TIMEOUT;
        }
        if (hasCause(error, ConnectException.class) || hasCause(error, UnknownHostException.class)) {
            return ResultCode.UPSTREAM_UNAVAILABLE;
        }
        if (error instanceof WebClientResponseException responseException) {
            return mapHttpStatus(responseException.getStatusCode().value());
        }
        if (error instanceof WebClientRequestException) {
            return ResultCode.UPSTREAM_UNAVAILABLE;
        }
        if (error != null && error.getClass().getSimpleName().contains("CallNotPermitted")) {
            return ResultCode.UPSTREAM_UNAVAILABLE;
        }
        return ResultCode.PROVIDER_ERROR;
    }

    public ResultCode mapHttpStatus(int status) {
        return switch (status) {
            case 408, 503, 504 -> ResultCode.UPSTREAM_TIMEOUT;
            case 429 -> ResultCode.RATE_LIMITED_UPSTREAM;
            default -> ResultCode.PROVIDER_ERROR;
        };
    }

    public boolean isRetryable(Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            return status == 408 || status == 429 || status >= 500;
        }
        return switch (map(error)) {
            case UPSTREAM_TIMEOUT, UPSTREAM_UNAVAILABLE, RATE_LIMITED_UPSTREAM -> true;
            default -> false;
        };
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        Throwable current = error;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
