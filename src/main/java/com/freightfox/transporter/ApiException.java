package com.freightfox.transporter;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

class ApiException extends RuntimeException {
    final int status;
    final String code;

    ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException exception) {
        return ResponseEntity.status(exception.status).body(new ErrorResponse("error", exception.code, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse("error", "VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> other(Exception exception) {
        return ResponseEntity.internalServerError().body(new ErrorResponse("error", "INTERNAL_ERROR", "An unexpected error occurred."));
    }
}
