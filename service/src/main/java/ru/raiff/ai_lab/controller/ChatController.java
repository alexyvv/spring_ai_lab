package ru.raiff.ai_lab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.raiff.ai_lab.dto.*;
import ru.raiff.ai_lab.model.Chat;
import ru.raiff.ai_lab.model.ChatEntry;
import ru.raiff.ai_lab.service.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    
    private final ChatService chatService;
    
    @GetMapping("/")
    public String index(Model model) {
        List<Chat> chats = chatService.getAllChats();
        model.addAttribute("chats", chats);
        model.addAttribute("chat", null);
        return "chat";
    }
    
    @GetMapping("/chat/{chatId}")
    public String viewChat(@PathVariable Long chatId, Model model) {
        Chat chat = chatService.getChatWithEntries(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        List<Chat> chats = chatService.getAllChats();
        model.addAttribute("chat", chat);
        model.addAttribute("chats", chats);
        return "chat";
    }
    
    @PostMapping("/chat/new")
    public String createNewChat(@RequestParam String title) {
        log.info("Creating new chat with title: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            title = "New Chat";
        }
        
        Chat chat = chatService.createChat(title);
        return "redirect:/chat/" + chat.getId();
    }
    
    @PostMapping("/chat/{chatId}/delete")
    public String deleteChat(@PathVariable Long chatId) {
        log.info("Deleting chat with id: {}", chatId);
        
        if (chatService.getChatById(chatId).isPresent()) {
            chatService.deleteChat(chatId);
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/chat/{chatId}/entry")
    public String addChatEntry(
            @PathVariable Long chatId,
            @RequestParam String prompt) {
        
        log.info("Adding entry to chat {}: content length={}", 
                chatId, prompt != null ? prompt.length() : 0);
        
        if (prompt != null && !prompt.trim().isEmpty()) {
            // Add user message
            chatService.addChatEntry(chatId, prompt, ChatEntry.Role.USER);
            
            // TODO: Here you would typically call your AI service to get a response
            // For now, let's add a simple response
            String assistantResponse = "Это симулированный ответ на: " + prompt;
            chatService.addChatEntry(chatId, assistantResponse, ChatEntry.Role.ASSISTANT);
        }
        
        return "redirect:/chat/" + chatId;
    }
    
    @GetMapping("/api/chat/{chatId}/entries")
    @ResponseBody
    public ResponseEntity<List<ChatEntryDto>> getChatEntries(@PathVariable Long chatId) {
        List<ChatEntry> entries = chatService.getChatEntries(chatId);
        List<ChatEntryDto> entryDtos = entries.stream()
                .map(this::convertToChatEntryDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(entryDtos);
    }
    
    @GetMapping("/api/chats")
    @ResponseBody
    public ResponseEntity<List<ChatDto>> getAllChats() {
        List<Chat> chats = chatService.getAllChats();
        List<ChatDto> chatDtos = chats.stream()
                .map(this::convertToChatDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chatDtos);
    }
    
    private ChatDto convertToChatDto(Chat chat) {
        ChatDto dto = new ChatDto();
        dto.setId(chat.getId());
        dto.setTitle(chat.getTitle());
        dto.setCreatedAt(chat.getCreatedAt());
        return dto;
    }
    
    private ChatDto convertToChatDtoWithEntries(Chat chat) {
        ChatDto dto = convertToChatDto(chat);
        if (chat.getEntries() != null) {
            List<ChatEntryDto> entryDtos = chat.getEntries().stream()
                    .map(this::convertToChatEntryDto)
                    .collect(Collectors.toList());
            dto.setEntries(entryDtos);
        }
        return dto;
    }
    
    private ChatEntryDto convertToChatEntryDto(ChatEntry entry) {
        ChatEntryDto dto = new ChatEntryDto();
        dto.setId(entry.getId());
        dto.setContent(entry.getContent());
        dto.setRole(entry.getRole().name());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}