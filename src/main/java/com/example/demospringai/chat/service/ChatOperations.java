package com.example.demospringai.chat.service;

import java.util.List;

import com.example.demospringai.chat.dto.HistoryEntryDto;
import com.example.demospringai.chat.dto.LessonResponse;

public interface ChatOperations {

    String chat(String endpoint, String message);

    LessonResponse lesson(String endpoint, String message);

    String chatWithTools(String endpoint, String message);

    List<HistoryEntryDto> history();
}
