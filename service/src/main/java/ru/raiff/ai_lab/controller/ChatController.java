package ru.raiff.ai_lab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import ru.raiff.ai_lab.dto.ChatDto;
import ru.raiff.ai_lab.dto.ChatEntryDto;
import ru.raiff.ai_lab.model.Chat;
import ru.raiff.ai_lab.model.ChatEntry;
import ru.raiff.ai_lab.service.AIService;
import ru.raiff.ai_lab.service.ChatService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    
    private final ChatService chatService;
    private final AIService aiService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
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
        
        // Ensure entries are loaded
        if (chat.getEntries() == null) {
            chat.setEntries(chatService.getChatEntries(chatId));
        }
        
        log.info("Loading chat {} with {} entries", chatId, chat.getEntries().size());
        
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
    public String talkToModel(@PathVariable Long chatId, @RequestParam String prompt) {
        
        log.info("Processing user prompt for chat {}: prompt=[{}]", chatId, prompt);
        
        if (prompt != null && !prompt.trim().isEmpty()) {
            try {
                // Add user message
                ChatEntry userEntry = chatService.addChatEntry(chatId, prompt, ChatEntry.Role.USER);
                log.info("User message saved with id: {}", userEntry.getId());
                
                // Get context from previous messages (optional)
                List<ChatEntry> previousEntries = chatService.getChatEntries(chatId);
                log.info("Retrieved {} previous entries for context", previousEntries.size());
                
                String context = buildContext(previousEntries, 5); // Last 5 messages for context
                log.info("Built context: {}", context);
                
                // Generate AI response
                log.info("Calling AI service...");
                String assistantResponse = !context.isEmpty() ?
                        aiService.generateResponseWithContext(prompt, context) :
                        aiService.generateResponse(prompt);
                
                log.info("AI response received: {}", assistantResponse);
                
                // Save assistant response
                ChatEntry assistantEntry = chatService.addChatEntry(chatId, assistantResponse, ChatEntry.Role.ASSISTANT);
                log.info("Assistant response saved with id: {}", assistantEntry.getId());
                
            } catch (Exception e) {
                log.error("Error processing chat entry", e);
            }
        } else {
            log.warn("Empty prompt received for chat {}", chatId);
        }
        
        return "redirect:/chat/" + chatId;
    }
    
    private String buildContext(List<ChatEntry> entries, int maxEntries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        
        int startIndex = Math.max(0, entries.size() - maxEntries);
        StringBuilder context = new StringBuilder();
        
        for (int i = startIndex; i < entries.size(); i++) {
            ChatEntry entry = entries.get(i);
            context.append(entry.getRole().name())
                   .append(": ")
                   .append(entry.getContent())
                   .append("\n");
        }
        
        return context.toString();
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
    
    @GetMapping(value = "/chat-stream/{chatId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamChat(@PathVariable Long chatId, 
                                  @RequestParam(required = false) String userPrompt) {
        
        log.info("Stream request for chat {} with prompt: {}", chatId, userPrompt);
        
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
        
        executorService.execute(() -> {
            try {
                if (userPrompt != null && !userPrompt.trim().isEmpty()) {
                    // Save user message
                    ChatEntry userEntry = chatService.addChatEntry(chatId, userPrompt, ChatEntry.Role.USER);
                    log.info("Saved user message: {}", userEntry.getId());
                    
                    // Get context
                    List<ChatEntry> previousEntries = chatService.getChatEntries(chatId);
                    String context = buildContext(previousEntries, 5);
                    
                    // Stream AI response using reactive stream
                    Flux<String> responseStream = context != null && !context.isEmpty() ?
                            aiService.streamResponseWithContext(userPrompt, context) :
                            aiService.streamResponse(userPrompt);
                    
                    StringBuilder fullResponse = new StringBuilder();
                    
                    responseStream.subscribe(
                        chunk -> {
                            try {
                                fullResponse.append(chunk);
                                // Send in format expected by frontend
                                String jsonData = String.format("{\"text\":\"%s\"}", 
                                        chunk.replace("\"", "\\\"")
                                              .replace("\n", "\\n")
                                              .replace("\r", "\\r"));
                                emitter.send(jsonData);
                                log.debug("Sent chunk: {}", chunk);
                            } catch (Exception e) {
                                log.error("Error sending chunk", e);
                            }
                        },
                        error -> {
                            log.error("Error in stream", error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                // Save complete assistant response
                                String finalResponse = fullResponse.toString();
                                chatService.addChatEntry(chatId, finalResponse, ChatEntry.Role.ASSISTANT);
                                log.info("Saved assistant response for chat {}", chatId);
                                
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("Error completing stream", e);
                                emitter.completeWithError(e);
                            }
                        }
                    );
                } else {
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("Error in streaming", e);
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
    
    @GetMapping("/api/chat/{chatId}/stream")
    @ResponseBody
    @CrossOrigin
    public Flux<String> streamChatFlux(@PathVariable Long chatId, 
                                        @RequestParam String prompt) {
        log.info("Flux stream request for chat {} with prompt: {}", chatId, prompt);
        
        return Flux.create(sink -> {
            try {
                // Save user message
                chatService.addChatEntry(chatId, prompt, ChatEntry.Role.USER);
                
                // Generate and stream response
                String response = aiService.generateResponse(prompt);
                
                // Stream word by word
                String[] words = response.split(" ");
                for (String word : words) {
                    sink.next(word + " ");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Save assistant response
                chatService.addChatEntry(chatId, response, ChatEntry.Role.ASSISTANT);
                
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}