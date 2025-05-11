
package com.track.controller;

import com.track.model.DTO;
import com.track.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<DTO.ExpenseResponse> addExpense(
            Authentication authentication,
            @RequestBody DTO.ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(authentication.getName(), request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DTO.ExpenseResponse> getExpense(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpense(id, authentication.getName()));
    }
    
    @GetMapping
    public ResponseEntity<List<DTO.ExpenseResponse>> getUserExpenses(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getUserExpenses(authentication.getName()));
    }
    
    @GetMapping("/month")
    public ResponseEntity<List<DTO.ExpenseResponse>> getUserExpensesByMonth(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getUserExpensesByMonth(
                authentication.getName(), year, month));
    }
    
    @GetMapping("/summary")
    public ResponseEntity<DTO.ExpenseSummaryResponse> getExpenseSummary(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getExpenseSummary(authentication.getName()));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DTO.ExpenseResponse> updateExpense(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody DTO.ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, authentication.getName(), request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            Authentication authentication,
            @PathVariable Long id) {
        expenseService.deleteExpense(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}