package uk.khusy.util.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record HttpErrorInfo(
        ZonedDateTime timestamp,
        HttpStatus httpStatus,
        String path,
        String message
) {
    public HttpErrorInfo() {
        this(null, null, null, null);
    }

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this(ZonedDateTime.now(), httpStatus, path, message);
    }

    public int status() {
        return httpStatus.value();
    }

    public String error() {
        return httpStatus.getReasonPhrase();
    }
}
