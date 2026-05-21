package com.ankit.elearning.exception;

import com.ankit.elearning.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String msg = ex.getMessage();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (msg != null) {
            String lower = msg.toLowerCase();
            if (lower.contains("not found")) status = HttpStatus.NOT_FOUND;
            if (msg.contains("Unauthorized") || msg.contains("Access denied")) status = HttpStatus.FORBIDDEN;
            if (msg.contains("Invalid password") ||
                    msg.contains("Invalid") ||
                    msg.contains("already submitted") ||
                    msg.contains("Time over") ||
                    msg.contains("required") ||
                    msg.contains("must") ||
                    msg.contains("expired") ||
                    msg.contains("already")) status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.getReasonPhrase(), msg, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex,
                                              HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.getReasonPhrase(),
                        "Validation failed", request.getRequestURI(), errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.getReasonPhrase(),
                        "Access denied", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.getReasonPhrase(),
                        "Something went wrong", request.getRequestURI()));
    }
}
