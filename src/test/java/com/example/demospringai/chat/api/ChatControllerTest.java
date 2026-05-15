package com.example.demospringai.chat.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.example.demospringai.chat.dto.ChatResponseDto;
import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;
import com.example.demospringai.chat.service.AiResponseFormatException;
import com.example.demospringai.chat.service.ChatOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;

class ChatControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(new TestChatOperations()))
                .setControllerAdvice(new ChatExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void chatReturnsAnswer() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Hola\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.answer").value("Respuesta"))
                .andExpect(jsonPath("$.conversationId").value("generated-conversation"));
    }

    @Test
    void chatAcceptsConversationId() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Hola\",\"conversationId\":\"curso-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Respuesta"))
                .andExpect(jsonPath("$.conversationId").value("curso-1"));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("El campo message es obligatorio"));
    }

    @Test
    void chatRejectsTooLongMessage() throws Exception {
        String longMessage = "a".repeat(2001);

        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"" + longMessage + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("El campo message no puede superar 2000 caracteres"));
    }

    @Test
    void chatRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    @Test
    void chatFromQueryRejectsBlankMessage() throws Exception {
        mockMvc.perform(get("/api/chat").param("message", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void toolsReturnsAnswerAndConversationId() throws Exception {
        mockMvc.perform(post("/api/chat/tools")
                        .contentType("application/json")
                        .content("{\"message\":\"Qué hora es?\",\"conversationId\":\"tools-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Respuesta con tools"))
                .andExpect(jsonPath("$.conversationId").value("tools-1"));
    }

    @Test
    void historyIsEmptyInitially() throws Exception {
        mockMvc.perform(get("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historyReturnsEntries() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatController(new TestChatOperationsWithHistory()))
                .setControllerAdvice(new ChatExceptionHandler())
                .build();

        mockMvc.perform(get("/api/chat/history").param("conversationId", "curso-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].endpoint").value("POST /api/chat"))
                .andExpect(jsonPath("$[0].message").value("Hola"))
                .andExpect(jsonPath("$[0].response").value("Respuesta"))
                .andExpect(jsonPath("$[0].timestamp").exists())
                .andExpect(jsonPath("$[0].conversationId").value("curso-1"));
    }

    @Test
    void lessonReturnsBadGatewayWhenAiResponseHasInvalidFormat() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ChatController(new TestChatOperationsWithInvalidLessonResponse()))
                .setControllerAdvice(new ChatExceptionHandler())
                .setValidator(validator())
                .build();

        mockMvc.perform(post("/api/chat/lesson")
                        .contentType("application/json")
                        .content("{\"message\":\"Explica ChatClient\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI_INVALID_RESPONSE"));
    }

    private static class TestChatOperations implements ChatOperations {

        @Override
        public ChatResponseDto chat(String endpoint, String message, String conversationId) {
            return new ChatResponseDto("Respuesta", conversationId == null ? "generated-conversation" : conversationId);
        }

        @Override
        public LessonResponse lesson(String endpoint, String message, String conversationId) {
            return new LessonResponse("Titulo", "Resumen", List.of("Idea"), "Ejercicio",
                    conversationId == null ? "generated-conversation" : conversationId);
        }

        @Override
        public ChatResponseDto chatWithTools(String endpoint, String message, String conversationId) {
            return new ChatResponseDto("Respuesta con tools",
                    conversationId == null ? "generated-conversation" : conversationId);
        }

        @Override
        public List<HistoryEntryDto> history(String conversationId) {
            return List.of();
        }
    }

    private static class TestChatOperationsWithHistory extends TestChatOperations {

        @Override
        public List<HistoryEntryDto> history(String conversationId) {
            return List.of(new HistoryEntryDto("POST /api/chat", "Hola", "Respuesta",
                    Instant.parse("2026-05-14T12:00:00Z"), conversationId));
        }
    }

    private static class TestChatOperationsWithInvalidLessonResponse extends TestChatOperations {

        @Override
        public LessonResponse lesson(String endpoint, String message, String conversationId) {
            throw new AiResponseFormatException("Respuesta inválida", new RuntimeException("JSON inválido"));
        }
    }

    private static LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
