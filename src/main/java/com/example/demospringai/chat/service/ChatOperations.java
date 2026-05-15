package com.example.demospringai.chat.service;

import java.util.List;

import com.example.demospringai.chat.dto.ChatResponseDto;
import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;

public interface ChatOperations {

    ChatResponseDto chat(String endpoint, String message, String conversationId);

    LessonResponse lesson(String endpoint, String message, String conversationId);

    ChatResponseDto chatWithTools(String endpoint, String message, String conversationId);

    List<HistoryEntryDto> history(String conversationId);
}
