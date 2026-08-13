package com.bloomframe.server.verification.controller;

import com.bloomframe.server.common.security.AuthenticatedUid;
import com.bloomframe.server.verification.exception.AuthWindowExpiredException;
import com.bloomframe.server.verification.exception.ReminderNotFoundException;
import com.bloomframe.server.verification.model.AuthTouchResult;
import com.bloomframe.server.verification.service.AuthTouchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTouchController {

    private final AuthTouchService authTouchService;

    public AuthTouchController(AuthTouchService authTouchService) {
        this.authTouchService = authTouchService;
    }

    @PostMapping("/api/v1/auth-touch")
    public AuthTouchResult authTouch(@AuthenticatedUid String uid, @RequestBody AuthTouchRequest request) {
        return authTouchService.touch(uid, request.reminderId());
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ReminderNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(AuthWindowExpiredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleExpired(AuthWindowExpiredException e) {
        return new ErrorResponse(e.getMessage());
    }

    public record AuthTouchRequest(String reminderId) {
    }

    public record ErrorResponse(String message) {
    }
}