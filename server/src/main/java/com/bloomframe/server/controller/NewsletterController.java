package com.bloomframe.server.controller;

import com.bloomframe.server.ai.NewsletterService;
import com.bloomframe.server.ai.dto.NewsletterDto;
import com.bloomframe.server.ai.dto.NewsletterGenerateRequest;
import com.bloomframe.server.common.security.AuthenticatedUid;
import com.bloomframe.server.verification.exception.UidMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{uid}/newsletters")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @GetMapping
    public List<NewsletterDto> list(
            @PathVariable String uid,
            @AuthenticatedUid String authenticatedUid) {
        requireSameUser(uid, authenticatedUid);
        return newsletterService.list(uid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewsletterDto generate(
            @PathVariable String uid,
            @AuthenticatedUid String authenticatedUid,
            @RequestBody(required = false) NewsletterGenerateRequest request) {
        requireSameUser(uid, authenticatedUid);
        return newsletterService.generate(uid, request);
    }

    @PostMapping("/{issueId}/send")
    public NewsletterDto send(
            @PathVariable String uid,
            @PathVariable String issueId,
            @AuthenticatedUid String authenticatedUid) {
        requireSameUser(uid, authenticatedUid);
        return newsletterService.send(uid, issueId);
    }

    private static void requireSameUser(String pathUid, String authenticatedUid) {
        if (!pathUid.equals(authenticatedUid)) {
            throw new UidMismatchException();
        }
    }
}
