package ru.raiff.ai_lab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.raiff.ai_lab.model.VectorStore;
import ru.raiff.ai_lab.repository.VectorStoreRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VectorStoreService {
    
    private final VectorStoreRepository vectorStoreRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public VectorStore saveVector(String id, String content, Map<String, Object> metadata, float[] embedding) {
        VectorStore vectorStore = new VectorStore();
        vectorStore.setId(id != null ? id : UUID.randomUUID().toString());
        vectorStore.setContent(content);
        vectorStore.setMetadata(convertMetadataToJson(metadata));
        vectorStore.setEmbedding(convertEmbeddingToString(embedding));
        
        log.info("Saving vector with id: {}", vectorStore.getId());
        return vectorStoreRepository.save(vectorStore);
    }
    
    public Optional<VectorStore> getVectorById(String id) {
        return vectorStoreRepository.findById(id);
    }
    
    public List<VectorStore> getAllVectors() {
        return vectorStoreRepository.findAll();
    }
    
    public List<VectorStore> findSimilarVectors(float[] queryEmbedding, int limit) {
        String embeddingString = convertEmbeddingToString(queryEmbedding);
        log.info("Finding {} nearest neighbors", limit);
        return vectorStoreRepository.findNearestNeighbors(embeddingString, limit);
    }
    
    public List<VectorStore> findVectorsWithinDistance(float[] queryEmbedding, double distance) {
        String embeddingString = convertEmbeddingToString(queryEmbedding);
        log.info("Finding vectors within distance: {}", distance);
        return vectorStoreRepository.findWithinDistance(embeddingString, distance);
    }
    
    public List<VectorStore> searchByContent(String keyword) {
        return vectorStoreRepository.findByContentContaining(keyword);
    }
    
    public List<VectorStore> searchByMetadata(Map<String, Object> metadataQuery) {
        String jsonQuery = convertMetadataToJson(metadataQuery);
        return vectorStoreRepository.findByMetadataContaining(jsonQuery);
    }
    
    public VectorStore updateVector(String id, String content, Map<String, Object> metadata, float[] embedding) {
        VectorStore vectorStore = vectorStoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vector not found with id: " + id));
        
        if (content != null) {
            vectorStore.setContent(content);
        }
        if (metadata != null) {
            vectorStore.setMetadata(convertMetadataToJson(metadata));
        }
        if (embedding != null) {
            vectorStore.setEmbedding(convertEmbeddingToString(embedding));
        }
        
        log.info("Updating vector with id: {}", id);
        return vectorStoreRepository.save(vectorStore);
    }
    
    public void deleteVector(String id) {
        log.info("Deleting vector with id: {}", id);
        vectorStoreRepository.deleteById(id);
    }
    
    public void deleteVectors(List<String> ids) {
        log.info("Deleting {} vectors", ids.size());
        vectorStoreRepository.deleteByIds(ids);
    }
    
    public void deleteAllVectors() {
        log.warn("Deleting all vectors");
        vectorStoreRepository.deleteAll();
    }
    
    public long countVectors() {
        return vectorStoreRepository.count();
    }
    
    public long countVectorsWithEmbedding() {
        return vectorStoreRepository.countVectorsWithEmbedding();
    }
    
    public boolean existsById(String id) {
        return vectorStoreRepository.existsById(id);
    }
    
    private String convertEmbeddingToString(float[] embedding) {
        if (embedding == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("Error converting metadata to JSON", e);
            return "{}";
        }
    }
    
    public Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing metadata JSON", e);
            return new HashMap<>();
        }
    }
}