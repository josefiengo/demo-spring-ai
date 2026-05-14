package com.example.demospringai.chat.service;

public class AiResponseFormatException extends RuntimeException {

    public AiResponseFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
