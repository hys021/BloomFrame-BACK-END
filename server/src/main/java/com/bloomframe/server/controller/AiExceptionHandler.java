package com.bloomframe.server.controller;

import com.bloomframe.server.ai.AiException;
import com.bloomframe.server.verification.exception.UidMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class AiExceptionHandler {

    @ExceptionHandler(UidMismatchException.class)
    public ResponseEntity<Map<String, String>> forbidden(UidMismatchException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        String message = e.getMessage() == null ? "bad request" : e.getMessage();
        HttpStatus status = message.toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Photo must be 5MB or smaller"));
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<Map<String, String>> ai(AiException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", e.getMessage()));
    }
}
