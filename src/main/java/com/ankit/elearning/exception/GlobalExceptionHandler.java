package com.ankit.elearning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (msg != null) {
            if (msg.contains("not found"))   status = HttpStatus.NOT_FOUND;
            if (msg.contains("Unauthorized")) status = HttpStatus.FORBIDDEN;
            if (msg.contains("Invalid password") ||
                    msg.contains("already submitted") ||
                    msg.contains("Time over"))   status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }
}