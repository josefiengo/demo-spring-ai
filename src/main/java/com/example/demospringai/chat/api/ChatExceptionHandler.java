package com.example.demospringai.chat.api;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.example.demospringai.chat.dto.ErrorResponseDto;
import com.example.demospringai.chat.service.AiResponseFormatException;

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

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("La solicitud no es válida");
        log.warn("chat.request.invalid error=\"{}\"", message);
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST", message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponseDto> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
        log.warn("chat.request.missing-param parameter=\"{}\"", exception.getParameterName());
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST",
                "El parámetro " + exception.getParameterName() + " es obligatorio"));
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ErrorResponseDto> handleInvalidRequest(InvalidRequestException exception) {
        log.warn("chat.request.invalid error=\"{}\"", exception.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<ErrorResponseDto> handleResourceAccess(ResourceAccessException exception) {
        if (hasCause(exception, SocketTimeoutException.class)) {
            log.warn("ai.response.timeout error=\"{}\"", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(new ErrorResponseDto("AI_TIMEOUT", """
                            Ollama no respondió a tiempo. Verificar que el modelo esté cargado o probar con un modelo más pequeño.
                            """.trim()));
        }
        if (hasCause(exception, ConnectException.class)) {
            log.warn("ai.response.connection-failed error=\"{}\"", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponseDto("AI_UNAVAILABLE",
                            "No se pudo conectar con Ollama. Verificar que el servicio esté iniciado."));
        }
        log.warn("ai.response.resource-access error=\"{}\"", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDto("AI_UNAVAILABLE",
                        "No se pudo obtener respuesta del modelo de IA. Revisar Ollama y el modelo configurado."));
    }

    @ExceptionHandler({ RestClientException.class, NonTransientAiException.class })
    ResponseEntity<ErrorResponseDto> handleAiClientError(RuntimeException exception) {
        if (isModelNotFound(exception)) {
            log.warn("ai.response.model-not-found error=\"{}\"", exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponseDto("AI_MODEL_NOT_FOUND",
                            "El modelo configurado no está disponible en Ollama. Descargarlo o cambiar la configuración."));
        }
        log.warn("ai.response.error error=\"{}\"", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDto("AI_UNAVAILABLE",
                        "No se pudo obtener respuesta del modelo de IA. Revisar Ollama y el modelo configurado."));
    }

    @ExceptionHandler(AiResponseFormatException.class)
    ResponseEntity<ErrorResponseDto> handleInvalidAiResponse(AiResponseFormatException exception) {
        log.warn("ai.response.invalid-format error=\"{}\"", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDto("AI_INVALID_RESPONSE",
                        "El modelo devolvió una respuesta que no cumple el formato esperado. Reintentar la solicitud."));
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ErrorResponseDto> handleUnexpected(RuntimeException exception) {
        log.error("chat.request.unexpected-error error=\"{}\"", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("INTERNAL_ERROR",
                        "Ocurrió un error inesperado al procesar la solicitud."));
    }

    private static boolean isModelNotFound(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("model") && normalized.contains("not found")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
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
