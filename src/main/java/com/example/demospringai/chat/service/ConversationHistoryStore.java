package com.example.demospringai.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.example.demospringai.chat.dto.HistoryEntryDto;

@Component
public class ConversationHistoryStore {

    static final int DEFAULT_MAX_ENTRIES_PER_CONVERSATION = 50;
    private static final int MAX_STORED_TEXT_LENGTH = 4_000;

    private final int maxEntriesPerConversation;
    private final ConcurrentMap<String, Queue<HistoryEntryDto>> entriesByConversation = new ConcurrentHashMap<>();

    public ConversationHistoryStore() {
        this(DEFAULT_MAX_ENTRIES_PER_CONVERSATION);
    }

    ConversationHistoryStore(int maxEntriesPerConversation) {
        this.maxEntriesPerConversation = maxEntriesPerConversation;
    }

    public String resolveConversationId(String requestedConversationId) {
        if (requestedConversationId == null || requestedConversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestedConversationId.trim();
    }

    public void record(String conversationId, String endpoint, String message, String response) {
        Queue<HistoryEntryDto> entries = entriesByConversation.computeIfAbsent(
                conversationId, key -> new ConcurrentLinkedQueue<>());
        entries.add(new HistoryEntryDto(
                endpoint,
                truncate(message),
                truncate(response),
                Instant.now(),
                conversationId));

        while (entries.size() > maxEntriesPerConversation) {
            entries.poll();
        }
    }

    public List<HistoryEntryDto> history(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }
        Queue<HistoryEntryDto> entries = entriesByConversation.get(conversationId.trim());
        if (entries == null) {
            return List.of();
        }
        return List.copyOf(entries);
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_STORED_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_STORED_TEXT_LENGTH) + "...";
    }
}
