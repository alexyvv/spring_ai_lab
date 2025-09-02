package ru.raiff.ai_lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.raiff.ai_lab.model.LoadedDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoadedDocumentRepository extends JpaRepository<LoadedDocument, Integer> {
    
    Optional<LoadedDocument> findByFilenameAndContentHash(String filename, String contentHash);
    
    List<LoadedDocument> findByFilename(String filename);
    
    List<LoadedDocument> findByDocumentType(String documentType);
    
    List<LoadedDocument> findByLoadedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ld FROM LoadedDocument ld WHERE ld.filename LIKE %:pattern%")
    List<LoadedDocument> findByFilenamePattern(@Param("pattern") String pattern);
    
    @Query("SELECT ld FROM LoadedDocument ld ORDER BY ld.loadedAt DESC")
    List<LoadedDocument> findAllOrderByLoadedAtDesc();
    
    @Query("SELECT COUNT(ld) FROM LoadedDocument ld WHERE ld.documentType = :type")
    Long countByDocumentType(@Param("type") String type);
    
    boolean existsByFilenameAndContentHash(String filename, String contentHash);
}