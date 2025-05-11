package com.track.service;

import com.track.model.DTO;
import com.track.model.Expense;
import com.track.model.MonthlyLimit;
import com.track.model.User;
import com.track.repository.ExpenseRepository;
import com.track.repository.MonthlyLimitRepository;
import com.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final MonthlyLimitRepository monthlyLimitRepository;
    
    @Transactional
    public DTO.ExpenseResponse addExpense(String username, DTO.ExpenseRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Expense expense = Expense.builder()
                .category(request.getCategory())
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDateTime.now())
                .user(user)
                .build();
        
        expense = expenseRepository.save(expense);
        
        // Check if monthly limit is exceeded
        checkMonthlyLimitExceeded(user.getId());
        
        return mapToExpenseResponse(expense);
    }
    
    public DTO.ExpenseResponse getExpense(Long id, String username) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        // Check if the expense belongs to the user
        if (!expense.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }
        
        return mapToExpenseResponse(expense);
    }
    
    public List<DTO.ExpenseResponse> getUserExpenses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        List<Expense> expenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(user.getId());
        
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .toList();
    }
    
    public List<DTO.ExpenseResponse> getUserExpensesByMonth(String username, int year, int month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(
                user.getId(), startDate, endDate);
        
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .toList();
    }
    
    @Transactional
    public DTO.ExpenseResponse updateExpense(Long id, String username, DTO.ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        // Check if the expense belongs to the user
        if (!expense.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }
        
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        
        expense = expenseRepository.save(expense);
        
        // Check if monthly limit is exceeded after update
        checkMonthlyLimitExceeded(expense.getUser().getId());
        
        return mapToExpenseResponse(expense);
    }
    
    @Transactional
    public void deleteExpense(Long id, String username) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        
        // Check if the expense belongs to the user
        if (!expense.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }
        
        expenseRepository.delete(expense);
    }
    
    public DTO.ExpenseSummaryResponse getExpenseSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Get current month's expenses
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        BigDecimal totalExpenses = expenseRepository.sumExpensesByUserIdAndDateRange(
                user.getId(), startDate, endDate);
        
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }
        
        // Get monthly limit if it exists
        Optional<MonthlyLimit> monthlyLimitOpt = monthlyLimitRepository.findByUserId(user.getId());
        BigDecimal monthlyLimit = monthlyLimitOpt.map(MonthlyLimit::getAmount)
                .orElse(BigDecimal.ZERO);
        
        // Calculate remaining budget
        BigDecimal remainingBudget = monthlyLimit.subtract(totalExpenses);
        
        // Check if limit is exceeded
        boolean limitExceeded = monthlyLimit.compareTo(BigDecimal.ZERO) > 0 && 
                totalExpenses.compareTo(monthlyLimit) > 0;
        
        return DTO.ExpenseSummaryResponse.builder()
                .totalExpenses(totalExpenses)
                .monthlyLimit(monthlyLimit)
                .remainingBudget(remainingBudget)
                .limitExceeded(limitExceeded)
                .build();
    }
    
    @Transactional
    public DTO.MonthlyLimitResponse setMonthlyLimit(String username, DTO.MonthlyLimitRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUserId(user.getId())
                .orElse(MonthlyLimit.builder().user(user).build());
        
        monthlyLimit.setAmount(request.getAmount());
        monthlyLimit = monthlyLimitRepository.save(monthlyLimit);
        
        // Check if current expenses already exceed the new limit
        checkMonthlyLimitExceeded(user.getId());
        
        return DTO.MonthlyLimitResponse.builder()
                .id(monthlyLimit.getId())
                .amount(monthlyLimit.getAmount())
                .createdAt(monthlyLimit.getCreatedAt())
                .updatedAt(monthlyLimit.getUpdatedAt())
                .build();
    }
    
    public DTO.MonthlyLimitResponse getMonthlyLimit(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        MonthlyLimit monthlyLimit = monthlyLimitRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (monthlyLimit == null) {
            return null;
        }
        
        return DTO.MonthlyLimitResponse.builder()
                .id(monthlyLimit.getId())
                .amount(monthlyLimit.getAmount())
                .createdAt(monthlyLimit.getCreatedAt())
                .updatedAt(monthlyLimit.getUpdatedAt())
                .build();
    }
    
    private void checkMonthlyLimitExceeded(Long userId) {
        // Logic to check if monthly limit is exceeded and send alert
        Optional<MonthlyLimit> monthlyLimitOpt = monthlyLimitRepository.findByUserId(userId);
        
        if (monthlyLimitOpt.isPresent()) {
            BigDecimal limit = monthlyLimitOpt.get().getAmount();
            
            // Get current month's expenses
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            
            BigDecimal totalExpenses = expenseRepository.sumExpensesByUserIdAndDateRange(
                    userId, startDate, endDate);
            
            if (totalExpenses != null && totalExpenses.compareTo(limit) > 0) {
                // Send alert logic would go here
                // This could be implemented with email services, push notifications, etc.
                System.out.println("ALERT: Monthly expense limit exceeded for user ID: " + userId);
            }
        }
    }
    
    private DTO.ExpenseResponse mapToExpenseResponse(Expense expense) {
        return DTO.ExpenseResponse.builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}