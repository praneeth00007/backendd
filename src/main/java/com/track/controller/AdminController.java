package com.track.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.track.model.DTO;
import com.track.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    
    @GetMapping("/users/count")
    public ResponseEntity<Long> getUserCount() {
        return ResponseEntity.ok(userService.getUserCount());
    }
    
    @GetMapping("/users")
    public ResponseEntity<Iterable<DTO.UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<DTO.UserProfileResponse> getUserDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @GetMapping("/users/{userId}/expenses")
    public ResponseEntity<Iterable<DTO.ExpenseResponse>> getUserExpenses(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserExpenses(userId));
    }
    
    @GetMapping("/users/{userId}/budgets")
    public ResponseEntity<DTO.MonthlyLimitResponse> getUserBudgets(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserMonthlyLimit(userId));
    }
    
}