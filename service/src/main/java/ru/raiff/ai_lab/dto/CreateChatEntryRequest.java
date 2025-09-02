package ru.raiff.ai_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatEntryRequest {
    private String content;
    private String role; // "USER" or "ASSISTANT"
}