
package com.track.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.track.model.Article;
import com.track.model.DTO;
import com.track.model.User;
import com.track.repository.ArticleRepository;
import com.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    
    public List<DTO.ArticleResponse> getPublishedArticles() {
        return articleRepository.findByPublishedTrueOrderByPublishedAtDesc().stream()
                .map(this::mapToArticleResponse)
                .toList();
    }
    
    public DTO.ArticleResponse getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        
        return mapToArticleResponse(article);
    }
    
    public List<DTO.ArticleResponse> getArticlesByAuthor(String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return articleRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId()).stream()
                .map(this::mapToArticleResponse)
                .toList();
    }
    
    @Transactional
    public DTO.ArticleResponse createArticle(String username, DTO.ArticleRequest request) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                // Convert base64 to file and upload to Cloudinary
                byte[] imageBytes = Base64.getDecoder().decode(
                        request.getImage().split(",")[1]
                );
                
                Map uploadResult = cloudinary.uploader().upload(
                        imageBytes,
                        ObjectUtils.asMap("folder", "expense-tracker/article-images")
                );
                
                imageUrl = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload article image", e);
            }
        }
        
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(imageUrl)
                .author(author)
                .published(false) // Only admin can publish
                .build();
        
        article = articleRepository.save(article);
        
        return mapToArticleResponse(article);
    }
    
    @Transactional
    public DTO.ArticleResponse updateArticle(Long id, String username, DTO.ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        
        // Check if the article belongs to the user or user is admin
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (!article.getAuthor().getUsername().equals(username) && !"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Unauthorized access to article");
        }
        
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                // Convert base64 to file and upload to Cloudinary
                byte[] imageBytes = Base64.getDecoder().decode(
                        request.getImage().split(",")[1]
                );
                
                Map uploadResult = cloudinary.uploader().upload(
                        imageBytes,
                        ObjectUtils.asMap("folder", "expense-tracker/article-images")
                );
                
                String imageUrl = (String) uploadResult.get("secure_url");
                article.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload article image", e);
            }
        }
        
        article = articleRepository.save(article);
        
        return mapToArticleResponse(article);
    }
    
    @Transactional
    public DTO.ArticleResponse publishArticle(Long id, String adminUsername) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        
        // Verify that the user is an admin
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (!"ADMIN".equals(admin.getRole())) {
            throw new IllegalArgumentException("Only admin can publish articles");
        }
        
        article.setPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        article = articleRepository.save(article);
        
        return mapToArticleResponse(article);
    }
    
    @Transactional
    public void deleteArticle(Long id, String username) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        
        // Check if the article belongs to the user or user is admin
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (!article.getAuthor().getUsername().equals(username) && !"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Unauthorized access to article");
        }
        
        articleRepository.delete(article);
    }
    
    private DTO.ArticleResponse mapToArticleResponse(Article article) {
        return DTO.ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .imageUrl(article.getImageUrl())
                .authorUsername(article.getAuthor().getUsername())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .published(article.isPublished())
                .build();
    }
}