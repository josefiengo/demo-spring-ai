package com.example.demospringai.chat.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import com.example.demospringai.ai.tools.DateTimeTools;
import com.example.demospringai.ai.tools.SpringAiConceptTools;
import com.example.demospringai.chat.dto.ChatResponseDto;
import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;
import com.example.demospringai.config.AiProperties;

@Service
public class ChatService implements ChatOperations {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_CONTEXT_ENTRIES = 6;
    private static final int MAX_CONTEXT_CHARS = 6_000;
    private static final String PLAIN_TEXT_INSTRUCTION =
            "Responder en español con texto plano, sin markdown, sin encabezados, sin listas con asteriscos ni guiones.\n";

    private static final BeanOutputConverter<LessonResponse> LESSON_CONVERTER =
            new BeanOutputConverter<>(LessonResponse.class);

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final SpringAiConceptTools springAiConceptTools;
    private final ConversationHistoryStore historyStore;
    private final String providerModel;

    public ChatService(
            ChatClient.Builder builder,
            DateTimeTools dateTimeTools,
            SpringAiConceptTools springAiConceptTools,
            ConversationHistoryStore historyStore,
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
        this.springAiConceptTools = springAiConceptTools;
        this.historyStore = historyStore;
        this.providerModel = ollama.model();
    }

    public ChatResponseDto chat(String endpoint, String message, String requestedConversationId) {
        String conversationId = historyStore.resolveConversationId(requestedConversationId);
        String answer = loggedAiCall(endpoint, conversationId, message, () -> chatContent(
                withConversationContext(conversationId, PLAIN_TEXT_INSTRUCTION), message));
        return new ChatResponseDto(answer, conversationId);
    }

    public LessonResponse lesson(String endpoint, String message, String requestedConversationId) {
        String conversationId = historyStore.resolveConversationId(requestedConversationId);
        LessonResponse lesson = loggedAiCall(endpoint, conversationId, message, () -> parseLessonResponse(chatClient.prompt()
                .system(withConversationContext(conversationId,
                        PLAIN_TEXT_INSTRUCTION + """
                        Devolver una lección breve sobre el tema solicitado.
                        Mantener topic, explanation y nextExercise en una oración corta.
                        Usar exactamente 3 keyIdeas, cada una de hasta 12 palabras.
                        """ + LESSON_CONVERTER.getFormat()))
                .user(message)
                .call()));
        return new LessonResponse(
                lesson.topic(),
                lesson.explanation(),
                lesson.keyIdeas(),
                lesson.nextExercise(),
                conversationId);
    }

    public ChatResponseDto chatWithTools(String endpoint, String message, String requestedConversationId) {
        String conversationId = historyStore.resolveConversationId(requestedConversationId);
        String answer = loggedAiCall(endpoint, conversationId, message, () -> chatClient.prompt()
                .system(withConversationContext(conversationId,
                        PLAIN_TEXT_INSTRUCTION + """
                        Usar herramientas cuando se necesiten datos actuales disponibles.
                        Si una herramienta devuelve fecha u hora, usar esos valores literalmente.
                        No recalcular ni inferir día de semana, zona horaria o calendario.
                        """))
                .user(message)
                .tools(dateTimeTools, springAiConceptTools)
                .call()
                .content());
        return new ChatResponseDto(answer, conversationId);
    }

    private <T> T loggedAiCall(String endpoint, String conversationId, String message, Supplier<T> call) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.nanoTime();

        log.info("ai.request.start id={} endpoint=\"{}\" conversationId={} provider={} model={} promptChars={}",
                requestId, endpoint, conversationId, "ollama", providerModel, message.length());

        try {
            T response = call.get();
            long elapsedMillis = elapsedMillis(start);
            log.info("ai.request.done id={} endpoint=\"{}\" elapsedMs={} responseType={} responseChars={}",
                    requestId, endpoint, elapsedMillis, responseType(response), responseChars(response));
            historyStore.record(conversationId, endpoint, message, String.valueOf(response));
            return response;
        }
        catch (RuntimeException exception) {
            long elapsedMillis = elapsedMillis(start);
            if (exception instanceof AiResponseFormatException formatException) {
                log.warn("ai.request.invalid-response id={} endpoint=\"{}\" elapsedMs={} error=\"{}\"",
                        requestId, endpoint, elapsedMillis, formatException.getMessage());
                throw formatException;
            }
            log.error("ai.request.failed id={} endpoint=\"{}\" elapsedMs={} error=\"{}\"",
                    requestId, endpoint, elapsedMillis, exception.getMessage(), exception);
            throw exception;
        }
    }

    @Override
    public List<HistoryEntryDto> history(String conversationId) {
        return historyStore.history(conversationId);
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String chatContent(String systemPrompt, String message) {
        ChatClient.ChatClientRequestSpec request = chatClient.prompt();
        if (!systemPrompt.isBlank()) {
            request = request.system(systemPrompt);
        }
        return request.user(message).call().content();
    }

    private String withConversationContext(String conversationId, String systemPrompt) {
        String context = conversationContext(conversationId);
        if (context.isBlank()) {
            return systemPrompt;
        }
        return systemPrompt + "\n" + context;
    }

    private String conversationContext(String conversationId) {
        List<HistoryEntryDto> entries = historyStore.history(conversationId);
        if (entries.isEmpty()) {
            return "";
        }

        int start = Math.max(0, entries.size() - MAX_CONTEXT_ENTRIES);
        StringBuilder context = new StringBuilder("""
                Historial reciente de esta conversación.
                Usarlo solo como contexto; la solicitud actual del usuario tiene prioridad.
                """);
        for (HistoryEntryDto entry : entries.subList(start, entries.size())) {
            appendWithinLimit(context, "Usuario: " + entry.message() + "\n");
            appendWithinLimit(context, "Asistente: " + entry.response() + "\n");
            if (context.length() >= MAX_CONTEXT_CHARS) {
                break;
            }
        }
        return context.toString();
    }

    private static void appendWithinLimit(StringBuilder target, String text) {
        int remainingChars = MAX_CONTEXT_CHARS - target.length();
        if (remainingChars <= 0) {
            return;
        }
        if (text.length() <= remainingChars) {
            target.append(text);
            return;
        }
        target.append(text, 0, remainingChars);
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

    private static LessonResponse parseLessonResponse(ChatClient.CallResponseSpec responseSpec) {
        String rawContent = responseSpec.content();
        try {
            return LESSON_CONVERTER.convert(rawContent);
        }
        catch (RuntimeException exception) {
            if (hasCause(exception, JsonProcessingException.class)) {
                log.warn("ai.response.invalid-format rawContent=\"{}\"", rawContent);
                throw new AiResponseFormatException("El modelo devolvió una respuesta estructurada inválida", exception);
            }
            throw exception;
        }
    }

    private static boolean hasCause(Throwable exception, Class<? extends Throwable> causeType) {
        Throwable current = exception;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
