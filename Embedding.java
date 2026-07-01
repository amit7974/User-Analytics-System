package com.example.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Stores a generated vector embedding for an {@link Event}. The vector is
 * persisted as a JSON array of doubles. In production this table would be
 * replaced (or mirrored) by a dedicated vector database such as Pinecone,
 * Milvus, Weaviate, or pgvector for ANN (approximate nearest neighbour) search.
 */
@Entity
@Table(name = "embeddings", indexes = {
        @Index(name = "idx_embeddings_event_id", columnList = "event_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Embedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vector", nullable = false, columnDefinition = "json")
    private String vector;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
