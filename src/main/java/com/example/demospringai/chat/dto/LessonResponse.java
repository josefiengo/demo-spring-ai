package com.example.demospringai.chat.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonResponse(
        String topic,
        String explanation,
        List<String> keyIdeas,
        String nextExercise,
        String conversationId
) {

    public LessonResponse(String topic, String explanation, List<String> keyIdeas, String nextExercise) {
        this(topic, explanation, keyIdeas, nextExercise, null);
    }
}
