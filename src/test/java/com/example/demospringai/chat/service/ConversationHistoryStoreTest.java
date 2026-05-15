package com.example.demospringai.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConversationHistoryStoreTest {

    @Test
    void historyIsSeparatedByConversationId() {
        ConversationHistoryStore store = new ConversationHistoryStore(10);

        store.record("curso-1", "POST /api/chat", "Hola", "Respuesta 1");
        store.record("curso-2", "POST /api/chat", "Hola", "Respuesta 2");

        assertThat(store.history("curso-1"))
                .hasSize(1)
                .first()
                .satisfies(entry -> {
                    assertThat(entry.response()).isEqualTo("Respuesta 1");
                    assertThat(entry.conversationId()).isEqualTo("curso-1");
                });
        assertThat(store.history("curso-2"))
                .hasSize(1)
                .first()
                .satisfies(entry -> assertThat(entry.response()).isEqualTo("Respuesta 2"));
    }

    @Test
    void historyKeepsOnlyConfiguredCapacity() {
        ConversationHistoryStore store = new ConversationHistoryStore(2);

        store.record("curso-1", "POST /api/chat", "Uno", "Respuesta 1");
        store.record("curso-1", "POST /api/chat", "Dos", "Respuesta 2");
        store.record("curso-1", "POST /api/chat", "Tres", "Respuesta 3");

        assertThat(store.history("curso-1"))
                .extracting("message")
                .containsExactly("Dos", "Tres");
    }

    @Test
    void blankConversationIdDoesNotExposeGlobalHistory() {
        ConversationHistoryStore store = new ConversationHistoryStore(10);

        store.record("curso-1", "POST /api/chat", "Hola", "Respuesta");

        assertThat(store.history(null)).isEmpty();
        assertThat(store.history(" ")).isEmpty();
    }
}
