package com.example.demospringai.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponseDto(
        String answer,
        String conversationId
) {

    public ChatResponseDto(String answer) {
        this(answer, null);
    }
}
