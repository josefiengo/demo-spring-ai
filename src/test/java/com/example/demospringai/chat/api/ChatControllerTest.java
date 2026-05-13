package com.example.demospringai.chat.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.demospringai.chat.dto.LessonResponse;
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
                .andExpect(jsonPath("$.answer").value("Respuesta"));
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
    void chatRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("{\"message\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    private static class TestChatOperations implements ChatOperations {

        @Override
        public String chat(String endpoint, String message) {
            return "Respuesta";
        }

        @Override
        public LessonResponse lesson(String endpoint, String message) {
            return new LessonResponse("Titulo", "Resumen", List.of("Idea"), "Ejercicio");
        }

        @Override
        public String chatWithTools(String endpoint, String message) {
            return "Respuesta con tools";
        }
    }
}
