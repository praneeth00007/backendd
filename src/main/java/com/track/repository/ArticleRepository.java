package com.track.repository;

import com.track.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByPublishedTrueOrderByPublishedAtDesc();
    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}