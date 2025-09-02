package ru.raiff.ai_lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.raiff.ai_lab.model.VectorStore;

import java.util.List;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStore, String> {
    
    @Query(value = "SELECT * FROM vector_store ORDER BY embedding <-> CAST(:embedding AS vector) LIMIT :limit", 
           nativeQuery = true)
    List<VectorStore> findNearestNeighbors(@Param("embedding") String embedding, @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM vector_store WHERE embedding <-> CAST(:embedding AS vector) < :distance", 
           nativeQuery = true)
    List<VectorStore> findWithinDistance(@Param("embedding") String embedding, @Param("distance") double distance);
    
    @Query("SELECT vs FROM VectorStore vs WHERE vs.content LIKE %:keyword%")
    List<VectorStore> findByContentContaining(@Param("keyword") String keyword);
    
    @Query(value = "SELECT * FROM vector_store WHERE metadata @> CAST(:json AS json)", nativeQuery = true)
    List<VectorStore> findByMetadataContaining(@Param("json") String json);
    
    @Modifying
    @Query("DELETE FROM VectorStore vs WHERE vs.id IN :ids")
    void deleteByIds(@Param("ids") List<String> ids);
    
    @Query(value = "SELECT COUNT(*) FROM vector_store WHERE embedding IS NOT NULL", nativeQuery = true)
    Long countVectorsWithEmbedding();
}