package com.example.demospringai.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatRequestTest {

    @Test
    void createsRequestWithMessage() {
        ChatRequest request = new ChatRequest("Hola Spring AI");

        assertThat(request.message()).isEqualTo("Hola Spring AI");
    }
}
