package com.example.demospringai.chat.dto;

import java.time.Instant;

public record HistoryEntryDto(
        String endpoint,
        String message,
        String response,
        Instant timestamp
) {
}
