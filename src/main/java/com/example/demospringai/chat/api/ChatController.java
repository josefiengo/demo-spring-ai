package com.example.demospringai.chat.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import com.example.demospringai.chat.dto.ChatRequest;
import com.example.demospringai.chat.dto.ChatResponseDto;
import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;
import com.example.demospringai.chat.service.ChatOperations;

@RestController
@RequestMapping("/api")
@Validated
public class ChatController {

    private static final int MAX_MESSAGE_LENGTH = 2_000;
    private static final int MAX_CONVERSATION_ID_LENGTH = 80;
    private static final java.util.regex.Pattern CONVERSATION_ID_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._:-]{0,79}$");

    private final ChatOperations chatOperations;

    public ChatController(ChatOperations chatOperations) {
        this.chatOperations = chatOperations;
    }

    @GetMapping("/health")
    public ChatResponseDto health() {
        return new ChatResponseDto("Spring Boot está listo. Configurar Ollama con gemma4 para usar /api/chat.");
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(@Valid @RequestBody ChatRequest request) {
        return chatOperations.chat("POST /api/chat", request.message(), request.conversationId());
    }

    @GetMapping("/chat")
    public ChatResponseDto chatFromQuery(
            @RequestParam
            @NotBlank(message = "El campo message es obligatorio")
            @Size(max = 2000, message = "El campo message no puede superar 2000 caracteres")
            String message,
            @RequestParam(required = false)
            @Size(max = 80, message = "El campo conversationId no puede superar 80 caracteres")
            @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._:-]{0,79}$",
                    message = "El campo conversationId solo permite letras, números, punto, guion, guion bajo y dos puntos")
            String conversationId) {
        validateQueryRequest(message, conversationId);
        return chatOperations.chat("GET /api/chat", message, conversationId);
    }

    @PostMapping("/chat/lesson")
    public LessonResponse lesson(@Valid @RequestBody ChatRequest request) {
        return chatOperations.lesson("POST /api/chat/lesson", request.message(), request.conversationId());
    }

    @PostMapping("/chat/tools")
    public ChatResponseDto tools(@Valid @RequestBody ChatRequest request) {
        return chatOperations.chatWithTools("POST /api/chat/tools", request.message(), request.conversationId());
    }

    @GetMapping("/chat/history")
    public List<HistoryEntryDto> history(@RequestParam(required = false) String conversationId) {
        return chatOperations.history(conversationId);
    }

    private static void validateQueryRequest(String message, String conversationId) {
        if (message == null || message.isBlank()) {
            throw new InvalidRequestException("El campo message es obligatorio");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidRequestException("El campo message no puede superar 2000 caracteres");
        }
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        if (conversationId.length() > MAX_CONVERSATION_ID_LENGTH) {
            throw new InvalidRequestException("El campo conversationId no puede superar 80 caracteres");
        }
        if (!CONVERSATION_ID_PATTERN.matcher(conversationId).matches()) {
            throw new InvalidRequestException(
                    "El campo conversationId solo permite letras, números, punto, guion, guion bajo y dos puntos");
        }
    }
}
