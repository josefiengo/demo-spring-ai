package com.example.demospringai.chat.service;

import com.example.demospringai.chat.dto.LessonResponse;

public interface ChatOperations {

    String chat(String endpoint, String message);

    LessonResponse lesson(String endpoint, String message);

    String chatWithTools(String endpoint, String message);
}
