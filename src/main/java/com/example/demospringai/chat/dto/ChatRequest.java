package com.example.demospringai.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "El campo message es obligatorio")
        String message
) {
}
