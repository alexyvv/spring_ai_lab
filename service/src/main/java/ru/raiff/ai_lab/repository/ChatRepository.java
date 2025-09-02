package ru.raiff.ai_lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.raiff.ai_lab.model.Chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    List<Chat> findByTitleContainingIgnoreCase(String title);
    
    List<Chat> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.entries WHERE c.id = :id")
    Optional<Chat> findByIdWithEntries(Long id);
    
    @Query("SELECT c FROM Chat c ORDER BY c.createdAt DESC")
    List<Chat> findAllOrderByCreatedAtDesc();
}