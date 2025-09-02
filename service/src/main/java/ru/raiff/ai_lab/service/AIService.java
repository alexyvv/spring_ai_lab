package ru.raiff.ai_lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {
    
    private final ChatClient chatClient;
    
    public String generateResponse(String prompt) {
        log.info("Generating AI response for prompt: {}", prompt);
        
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            log.info("AI response generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating AI response", e);
            return "Извините, произошла ошибка при генерации ответа: " + e.getMessage();
        }
    }
    
    public String generateResponseWithContext(String prompt, String context) {
        log.info("Generating AI response with context");
        
        try {
            String fullPrompt = context != null ? 
                    "Context: " + context + "\n\nUser: " + prompt : 
                    prompt;
            
            String response = chatClient.prompt()
                    .user(fullPrompt)
                    .call()
                    .content();
            
            log.info("AI response with context generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating AI response with context", e);
            return "Извините, произошла ошибка при генерации ответа: " + e.getMessage();
        }
    }
}