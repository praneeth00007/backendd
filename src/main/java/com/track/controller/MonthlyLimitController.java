package com.track.controller;

import com.track.model.DTO;
import com.track.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class MonthlyLimitController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<DTO.MonthlyLimitResponse> setMonthlyLimit(
            Authentication authentication,
            @RequestBody DTO.MonthlyLimitRequest request) {
        return ResponseEntity.ok(expenseService.setMonthlyLimit(authentication.getName(), request));
    }
    
    @GetMapping
    public ResponseEntity<DTO.MonthlyLimitResponse> getMonthlyLimit(Authentication authentication) {
        DTO.MonthlyLimitResponse response = expenseService.getMonthlyLimit(authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
