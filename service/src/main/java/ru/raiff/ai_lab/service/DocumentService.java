package ru.raiff.ai_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.raiff.ai_lab.model.LoadedDocument;
import ru.raiff.ai_lab.repository.LoadedDocumentRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {
    
    private final LoadedDocumentRepository loadedDocumentRepository;
    
    public LoadedDocument saveDocument(String filename, String content, String documentType, Integer chunkCount) {
        String contentHash = calculateHash(content);
        
        if (loadedDocumentRepository.existsByFilenameAndContentHash(filename, contentHash)) {
            log.info("Document already exists: {} with hash: {}", filename, contentHash);
            return loadedDocumentRepository.findByFilenameAndContentHash(filename, contentHash)
                    .orElseThrow(() -> new RuntimeException("Document not found after existence check"));
        }
        
        LoadedDocument document = new LoadedDocument();
        document.setFilename(filename);
        document.setContentHash(contentHash);
        document.setDocumentType(documentType);
        document.setChunkCount(chunkCount);
        document.setLoadedAt(LocalDateTime.now());
        
        log.info("Saving new document: {} of type: {}", filename, documentType);
        return loadedDocumentRepository.save(document);
    }
    
    public Optional<LoadedDocument> getDocumentById(Integer id) {
        return loadedDocumentRepository.findById(id);
    }
    
    public Optional<LoadedDocument> getDocumentByFilenameAndHash(String filename, String contentHash) {
        return loadedDocumentRepository.findByFilenameAndContentHash(filename, contentHash);
    }
    
    public List<LoadedDocument> getDocumentsByFilename(String filename) {
        return loadedDocumentRepository.findByFilename(filename);
    }
    
    public List<LoadedDocument> getDocumentsByType(String documentType) {
        return loadedDocumentRepository.findByDocumentType(documentType);
    }
    
    public List<LoadedDocument> searchDocumentsByPattern(String pattern) {
        return loadedDocumentRepository.findByFilenamePattern(pattern);
    }
    
    public List<LoadedDocument> getRecentDocuments() {
        return loadedDocumentRepository.findAllOrderByLoadedAtDesc();
    }
    
    public List<LoadedDocument> getDocumentsInDateRange(LocalDateTime start, LocalDateTime end) {
        return loadedDocumentRepository.findByLoadedAtBetween(start, end);
    }
    
    public boolean isDocumentLoaded(String filename, String content) {
        String contentHash = calculateHash(content);
        return loadedDocumentRepository.existsByFilenameAndContentHash(filename, contentHash);
    }
    
    public void deleteDocument(Integer id) {
        log.info("Deleting document with id: {}", id);
        loadedDocumentRepository.deleteById(id);
    }
    
    public void deleteAllDocuments() {
        log.warn("Deleting all documents");
        loadedDocumentRepository.deleteAll();
    }
    
    public long countDocuments() {
        return loadedDocumentRepository.count();
    }
    
    public long countDocumentsByType(String documentType) {
        return loadedDocumentRepository.countByDocumentType(documentType);
    }
    
    public LoadedDocument updateChunkCount(Integer documentId, Integer newChunkCount) {
        LoadedDocument document = loadedDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        document.setChunkCount(newChunkCount);
        log.info("Updating document {} chunk count to: {}", documentId, newChunkCount);
        return loadedDocumentRepository.save(document);
    }
    
    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }
}