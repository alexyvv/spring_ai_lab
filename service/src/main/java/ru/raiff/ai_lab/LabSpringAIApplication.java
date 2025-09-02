package ru.raiff.ai_lab;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class LabSpringAIApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(LabSpringAIApplication.class, args);
//        final ChatClient chatClient = context.getBean(ChatClient.class);
//        String answer = chatClient.prompt("дай первую строчку Bohemian Rhapsody")
//                .options(OllamaOptions.builder().build())
//                .call()
//                .content();
//        System.out.println(answer);
    }
}
