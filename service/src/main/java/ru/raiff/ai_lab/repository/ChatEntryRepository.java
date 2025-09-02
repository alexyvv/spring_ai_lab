package ru.raiff.ai_lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.raiff.ai_lab.model.ChatEntry;

import java.util.List;

@Repository
public interface ChatEntryRepository extends JpaRepository<ChatEntry, Long> {
    
    List<ChatEntry> findByChatId(Long chatId);
    
    List<ChatEntry> findByChatIdOrderByCreatedAtAsc(Long chatId);
    
    List<ChatEntry> findByRole(ChatEntry.Role role);
    
    @Query("SELECT ce FROM ChatEntry ce WHERE ce.chat.id = :chatId AND ce.role = :role")
    List<ChatEntry> findByChatIdAndRole(@Param("chatId") Long chatId, @Param("role") ChatEntry.Role role);
    
    @Query("SELECT ce FROM ChatEntry ce WHERE ce.content LIKE %:keyword%")
    List<ChatEntry> findByContentContaining(@Param("keyword") String keyword);
    
    void deleteByChatId(Long chatId);
}