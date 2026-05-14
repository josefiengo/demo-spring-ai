package com.example.demospringai.chat.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.example.demospringai.chat.dto.ErrorResponseDto;

@RestControllerAdvice
public class ChatExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponseDto> handleInvalidJson(HttpMessageNotReadableException exception) {
        log.warn("chat.request.invalid-json error=\"{}\"", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDto("INVALID_JSON",
                        "El body debe ser JSON válido en UTF-8 con el formato: {\"message\":\"Hola\"}"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("La solicitud no es válida");
        log.warn("chat.request.invalid error=\"{}\"", message);
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST", message));
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<ErrorResponseDto> handleTimeout(ResourceAccessException exception) {
        log.warn("ai.response.timeout error=\"{}\"", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponseDto("AI_TIMEOUT", """
                        Ollama no respondió a tiempo. Verificar que el modelo esté cargado o probar con un modelo más pequeño.
                        """.trim()));
    }

    @ExceptionHandler({ RestClientException.class, NonTransientAiException.class })
    ResponseEntity<ErrorResponseDto> handleAiClientError(RuntimeException exception) {
        log.warn("ai.response.error error=\"{}\"", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDto("AI_UNAVAILABLE",
                        "No se pudo obtener respuesta del modelo de IA. Revisar Ollama y el modelo configurado."));
    }
}
