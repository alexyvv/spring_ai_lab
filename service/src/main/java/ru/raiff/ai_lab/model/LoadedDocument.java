package ru.raiff.ai_lab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "loaded_document", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"filename", "content_hash"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadedDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    
    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;
    
    @Column(name = "chunk_count")
    private Integer chunkCount;
    
    @Column(name = "loaded_at", nullable = false)
    private LocalDateTime loadedAt;
    
    @PrePersist
    protected void onCreate() {
        if (loadedAt == null) {
            loadedAt = LocalDateTime.now();
        }
    }
}