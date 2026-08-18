package com.bloomframe.server.verification.controller;

import com.bloomframe.server.common.security.AuthenticatedUid;
import com.bloomframe.server.verification.exception.UidMismatchException;
import com.bloomframe.server.verification.model.VerificationLog;
import com.bloomframe.server.verification.service.AuthTouchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class AuthLogController {

    private final AuthTouchService authTouchService;

    public AuthLogController(AuthTouchService authTouchService) {
        this.authTouchService = authTouchService;
    }

    @GetMapping("/api/v1/users/{uid}/auth-logs")
    public List<VerificationLog> getAuthLogs(
            @PathVariable String uid,
            @AuthenticatedUid String authenticatedUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        if (!uid.equals(authenticatedUid)) {
            throw new UidMismatchException();
        }
        return authTouchService.getLogs(uid, from, to);
    }

    @ExceptionHandler(UidMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleMismatch(UidMismatchException e) {
        return new ErrorResponse(e.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}