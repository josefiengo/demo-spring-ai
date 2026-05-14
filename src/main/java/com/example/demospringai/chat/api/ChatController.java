package com.example.demospringai.chat.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.demospringai.chat.dto.ChatRequest;
import com.example.demospringai.chat.dto.ChatResponseDto;
import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;
import com.example.demospringai.chat.service.ChatOperations;

@RestController
@RequestMapping("/api")
public class ChatController {

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
        return new ChatResponseDto(chatOperations.chat("POST /api/chat", request.message()));
    }

    @GetMapping("/chat")
    public ChatResponseDto chatFromQuery(@RequestParam String message) {
        return new ChatResponseDto(chatOperations.chat("GET /api/chat", message));
    }

    @PostMapping("/chat/lesson")
    public LessonResponse lesson(@Valid @RequestBody ChatRequest request) {
        return chatOperations.lesson("POST /api/chat/lesson", request.message());
    }

    @PostMapping("/chat/tools")
    public ChatResponseDto tools(@Valid @RequestBody ChatRequest request) {
        return new ChatResponseDto(chatOperations.chatWithTools("POST /api/chat/tools", request.message()));
    }

    @GetMapping("/chat/history")
    public List<HistoryEntryDto> history() {
        return chatOperations.history();
    }
}
