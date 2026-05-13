package com.example.demospringai.chat.dto;

import java.util.List;

public record LessonResponse(
        String topic,
        String explanation,
        List<String> keyIdeas,
        String nextExercise
) {
}
