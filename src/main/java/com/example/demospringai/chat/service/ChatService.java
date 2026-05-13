package com.example.demospringai.chat.service;

import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import com.example.demospringai.ai.tools.DateTimeTools;
import com.example.demospringai.chat.dto.LessonResponse;
import com.example.demospringai.config.AiProperties;

@Service
public class ChatService implements ChatOperations {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int PREVIEW_MAX_LENGTH = 120;

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final String providerModel;

    public ChatService(
            ChatClient.Builder builder,
            DateTimeTools dateTimeTools,
            AiProperties aiProperties) {
        AiProperties.Ollama ollama = aiProperties.ollama();
        builder.defaultOptions(OllamaChatOptions.builder()
                .disableThinking()
                .model(ollama.model())
                .temperature(ollama.temperature())
                .seed(ollama.seed())
                .topK(ollama.topK())
                .topP(ollama.topP())
                .numCtx(ollama.numCtx())
                .numPredict(ollama.numPredict())
                .keepAlive(ollama.keepAlive())
                .build());
        this.chatClient = builder.build();
        this.dateTimeTools = dateTimeTools;
        this.providerModel = ollama.model();
    }

    public String chat(String endpoint, String message) {
        return loggedAiCall(endpoint, message, () -> chatClient.prompt()
                .system("Responder en español claro y directo. El usuario está aprendiendo Spring AI.")
                .user(message)
                .call()
                .content());
    }

    public LessonResponse lesson(String endpoint, String message) {
        return loggedAiCall(endpoint, message, () -> chatClient.prompt()
                .system("""
                        Eres un mentor senior de Java y Spring Boot.
                        Responder siempre en español claro, con explicaciones cortas y prácticas.
                        Devolver una lección breve sobre el tema solicitado.
                        """)
                .user(message)
                .call()
                .entity(LessonResponse.class));
    }

    public String chatWithTools(String endpoint, String message) {
        return loggedAiCall(endpoint, message, () -> chatClient.prompt()
                .system("Responder en español. Usar herramientas cuando se necesiten datos actuales.")
                .user(message)
                .tools(dateTimeTools)
                .call()
                .content());
    }

    private <T> T loggedAiCall(String endpoint, String message, Supplier<T> call) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.nanoTime();

        log.info("ai.request.start id={} endpoint=\"{}\" provider={} model={} promptChars={} promptPreview=\"{}\"",
                requestId, endpoint, "ollama", providerModel, message.length(), preview(message));

        try {
            T response = call.get();
            long elapsedMillis = elapsedMillis(start);
            log.info("ai.request.done id={} endpoint=\"{}\" elapsedMs={} responseType={} responseChars={}",
                    requestId, endpoint, elapsedMillis, responseType(response), responseChars(response));
            return response;
        }
        catch (RuntimeException exception) {
            long elapsedMillis = elapsedMillis(start);
            log.error("ai.request.failed id={} endpoint=\"{}\" elapsedMs={} error=\"{}\"",
                    requestId, endpoint, elapsedMillis, exception.getMessage(), exception);
            throw exception;
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static String preview(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PREVIEW_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, PREVIEW_MAX_LENGTH) + "...";
    }

    private static String responseType(Object response) {
        return response == null ? "null" : response.getClass().getSimpleName();
    }

    private static int responseChars(Object response) {
        if (response == null) {
            return 0;
        }
        if (response instanceof String text) {
            return text.length();
        }
        return response.toString().length();
    }
}
