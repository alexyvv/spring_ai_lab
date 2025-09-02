package ru.raiff.ai_lab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vector_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorStore {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
    
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embedding;
}