package com.example.demospringai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "El campo message es obligatorio")
        @Size(max = 2000, message = "El campo message no puede superar 2000 caracteres")
        String message,

        @Size(max = 80, message = "El campo conversationId no puede superar 80 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._:-]{0,79}$",
                message = "El campo conversationId solo permite letras, números, punto, guion, guion bajo y dos puntos")
        String conversationId
) {

    public ChatRequest(String message) {
        this(message, null);
    }
}
