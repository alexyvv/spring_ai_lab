package ru.raiff.ai_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.raiff.ai_lab.model.Chat;
import ru.raiff.ai_lab.model.ChatEntry;
import ru.raiff.ai_lab.repository.ChatEntryRepository;
import ru.raiff.ai_lab.repository.ChatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final ChatEntryRepository chatEntryRepository;
    
    public Chat createChat(String title) {
        Chat chat = new Chat();
        chat.setTitle(title);
        chat.setCreatedAt(LocalDateTime.now());
        log.info("Creating new chat with title: {}", title);
        return chatRepository.save(chat);
    }
    
    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }
    
    public Optional<Chat> getChatWithEntries(Long id) {
        Optional<Chat> chatOpt = chatRepository.findByIdWithEntries(id);
        chatOpt.ifPresent(chat -> {
            log.info("Loaded chat {} with {} entries", chat.getId(), 
                    chat.getEntries() != null ? chat.getEntries().size() : 0);
        });
        return chatOpt;
    }
    
    public List<Chat> getAllChats() {
        return chatRepository.findAllOrderByCreatedAtDesc();
    }
    
    public List<Chat> searchChatsByTitle(String title) {
        return chatRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public Chat updateChatTitle(Long chatId, String newTitle) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        chat.setTitle(newTitle);
        log.info("Updating chat {} title to: {}", chatId, newTitle);
        return chatRepository.save(chat);
    }
    
    public ChatEntry addChatEntry(Long chatId, String content, ChatEntry.Role role) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        
        ChatEntry entry = new ChatEntry();
        entry.setContent(content);
        entry.setRole(role);
        entry.setChat(chat);
        entry.setCreatedAt(LocalDateTime.now());
        
        log.info("Adding {} entry to chat {}", role, chatId);
        return chatEntryRepository.save(entry);
    }
    
    public List<ChatEntry> getChatEntries(Long chatId) {
        return chatEntryRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
    
    public List<ChatEntry> getChatEntriesByRole(Long chatId, ChatEntry.Role role) {
        return chatEntryRepository.findByChatIdAndRole(chatId, role);
    }
    
    public List<ChatEntry> searchEntriesByContent(String keyword) {
        return chatEntryRepository.findByContentContaining(keyword);
    }
    
    public void deleteChat(Long chatId) {
        log.info("Deleting chat with id: {}", chatId);
        chatRepository.deleteById(chatId);
    }
    
    public void deleteChatEntry(Long entryId) {
        log.info("Deleting chat entry with id: {}", entryId);
        chatEntryRepository.deleteById(entryId);
    }
    
    public void deleteAllChatEntries(Long chatId) {
        log.info("Deleting all entries for chat: {}", chatId);
        chatEntryRepository.deleteByChatId(chatId);
    }
    
    public long countChats() {
        return chatRepository.count();
    }
    
    public long countChatEntries(Long chatId) {
        return chatEntryRepository.findByChatId(chatId).size();
    }
}