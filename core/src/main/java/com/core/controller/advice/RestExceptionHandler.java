package com.core.controller.advice;

import com.core.dto.ApiError;
import com.core.exception.NotFoundResourceException;
import com.core.exception.UniqueConstraintException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(NotFoundResourceException.class)
    public ResponseEntity<ApiError> handleNotFoundResourceException(NotFoundResourceException ex) {
        logger.info("resource not found", ex);
        var error = new ApiError.Builder()
                .status(NOT_FOUND)
                .date(LocalDateTime.now())
                .messages(List.of(ex.getMessage()))
                .build();

        return ResponseEntity.status(NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<ApiError> handleUniqueConstraintException(UniqueConstraintException ex) {
        logger.info("unique constraint exception", ex);
        var error = new ApiError.Builder()
                .status(CONFLICT)
                .date(LocalDateTime.now())
                .messages(List.of(ex.getMessage()))
                .build();

        return ResponseEntity.status(CONFLICT)
                .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.info("bad request", ex);
        List<String> messages = ex.getConstraintViolations()
                .stream()
                .map(this::extractMessages)
                .toList();
        var error = new ApiError.Builder()
                .status(BAD_REQUEST)
                .date(LocalDateTime.now())
                .messages(messages)
                .build();

        return ResponseEntity.badRequest()
                .body(error);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status,
                                                                  @NonNull WebRequest request) {
        logger.error("not valid request arguments", ex);
        List<String> messages = ex.getFieldErrors()
                .stream()
                .map(this::extractMessages)
                .toList();
        var error = new ApiError.Builder()
                .status(BAD_REQUEST)
                .date(LocalDateTime.now())
                .messages(messages)
                .build();

        return ResponseEntity.badRequest()
                .body(error);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status,
                                                                  @NonNull WebRequest request) {
        logger.error("not readable request body", ex);
        var error = new ApiError.Builder()
                .status(BAD_REQUEST)
                .date(LocalDateTime.now())
                .messages(List.of("not readable request body"))
                .build();

        return ResponseEntity.badRequest()
                .body(error);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatus status,
                                                                     @NonNull WebRequest request) {
        logger.error("media type not supported", ex);
        var error = new ApiError.Builder()
                .status(UNSUPPORTED_MEDIA_TYPE)
                .date(LocalDateTime.now())
                .messages(List.of("media type not supported"))
                .build();

        return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE)
                .body(error);
    }

    private String extractMessages(ConstraintViolation<?> violation) {
        var message = violation.getMessage();
        var propertyPath = violation.getPropertyPath();

        return String.format("property: %s, has error: %s", propertyPath, message);
    }

    private String extractMessages(FieldError error) {
        var message = error.getDefaultMessage();
        var field = error.getField();

        return String.format("field: %s, has error: %s", field, message);
    }
}
