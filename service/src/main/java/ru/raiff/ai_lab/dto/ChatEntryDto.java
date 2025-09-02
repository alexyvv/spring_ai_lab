package ru.raiff.ai_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEntryDto {
    private Long id;
    private String content;
    private String role;
    private LocalDateTime createdAt;
}