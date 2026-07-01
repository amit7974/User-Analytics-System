package com.example.analytics.repository;

import com.example.analytics.entity.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {

    Optional<Embedding> findByEventId(Long eventId);
}
