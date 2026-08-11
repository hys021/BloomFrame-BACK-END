package com.bloomframe.server.controller;

import com.bloomframe.server.dto.UserProfileDto;
import com.bloomframe.server.firebase.FirestoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{uid}/profile")
public class UserProfileController {

    private final FirestoreService firestoreService;

    public UserProfileController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String uid) {
        return firestoreService.getProfile(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public UserProfileDto saveProfile(@PathVariable String uid, @Valid @RequestBody UserProfileDto profile) {
        firestoreService.saveProfile(uid, profile);
        return profile;
    }
}
