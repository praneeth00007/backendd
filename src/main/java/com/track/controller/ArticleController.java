package com.track.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.track.model.DTO;

import com.track.service.ArticleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    @GetMapping("/public")
    public ResponseEntity<List<DTO.ArticleResponse>> getPublishedArticles() {
        return ResponseEntity.ok(articleService.getPublishedArticles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DTO.ArticleResponse> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticle(id));
    }
    
    @GetMapping("/author/{username}")
    public ResponseEntity<List<com.track.model.DTO.ArticleResponse>> getArticlesByAuthor(@PathVariable String username) {
        return ResponseEntity.ok(articleService.getArticlesByAuthor(username));
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<DTO.ArticleResponse>> getMyArticles(Authentication authentication) {
        return ResponseEntity.ok(articleService.getArticlesByAuthor(authentication.getName()));
    }
    
    @PostMapping
    public ResponseEntity<DTO.ArticleResponse> createArticle(
            Authentication authentication,
            @RequestBody DTO.ArticleRequest request) {
        return ResponseEntity.ok(articleService.createArticle(authentication.getName(), request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DTO.ArticleResponse> updateArticle(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody DTO.ArticleRequest request) {
        return ResponseEntity.ok(articleService.updateArticle(id, authentication.getName(), request));
    }
    
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DTO.ArticleResponse> publishArticle(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(articleService.publishArticle(id, authentication.getName()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(
            Authentication authentication,
            @PathVariable Long id) {
        articleService.deleteArticle(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}