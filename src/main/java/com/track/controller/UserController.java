package com.track.controller;

import com.track.model.DTO;
import com.track.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<DTO.UserProfileResponse> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<DTO.UserProfileResponse> updateUserProfile(
            Authentication authentication,
            @RequestBody DTO.UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(authentication.getName(), request));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Iterable<DTO.UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
