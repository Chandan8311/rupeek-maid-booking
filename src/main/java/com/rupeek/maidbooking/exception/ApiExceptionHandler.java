package com.rupeek.maidbooking.exception;

import java.time.Instant;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rupeek.maidbooking.constants.ErrorCode;
import com.rupeek.maidbooking.dto.common.ErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> business(BusinessException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .code(exception.getCode().name())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> invalid(Exception exception) {
        String message = exception instanceof MethodArgumentNotValidException invalid
                && invalid.getBindingResult().hasFieldErrors()
                ? invalid.getBindingResult().getFieldErrors().get(0).getField() + ": "
                  + invalid.getBindingResult().getFieldErrors().get(0).getDefaultMessage()
                : "Invalid request or unsupported value";
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .code(ErrorCode.INVALID_REQUEST.name())
                        .message(message)
                        .timestamp(Instant.now())
                        .build());
    }
}
