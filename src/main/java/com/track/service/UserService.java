package com.track.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.track.model.DTO;
import com.track.model.User;
import com.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    
    public DTO.UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return DTO.UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    @Transactional
    public DTO.UserProfileResponse updateUserProfile(String username, DTO.UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Update profile image if provided
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            try {
                // Convert base64 to file and upload to Cloudinary
                byte[] imageBytes = Base64.getDecoder().decode(
                        request.getProfileImage().split(",")[1]
                );
                
                Map uploadResult = cloudinary.uploader().upload(
                        imageBytes,
                        ObjectUtils.asMap("folder", "expense-tracker/profile-images")
                );
                
                String imageUrl = (String) uploadResult.get("secure_url");
                user.setProfileImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image", e);
            }
        }
        
        userRepository.save(user);
        
        return DTO.UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    public Iterable<DTO.UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> DTO.UserProfileResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .profileImageUrl(user.getProfileImageUrl())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    public Long getUserCount() {
        return userRepository.count();
    }

    public DTO.UserProfileResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return DTO.UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Iterable<DTO.ExpenseResponse> getUserExpenses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return user.getExpenses().stream()
                .map(expense -> DTO.ExpenseResponse.builder()
                        .id(expense.getId())
                        .description(expense.getDescription())
                        .amount(expense.getAmount())
                        .expenseDate(expense.getExpenseDate())
                        .category(expense.getCategory())
                        .build())
                .toList();
    }

    public DTO.MonthlyLimitResponse getUserMonthlyLimit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (user.getMonthlyLimit() == null) {
            return null;
        }
        
        return DTO.MonthlyLimitResponse.builder()
                .id(user.getMonthlyLimit().getId())
                .amount(user.getMonthlyLimit().getAmount())
                .startDate(user.getMonthlyLimit().getStartDate())
                .endDate(user.getMonthlyLimit().getEndDate())
                .build();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public DTO.UserProfileResponse updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.setRole(role);
        userRepository.save(user);
        
        return DTO.UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}