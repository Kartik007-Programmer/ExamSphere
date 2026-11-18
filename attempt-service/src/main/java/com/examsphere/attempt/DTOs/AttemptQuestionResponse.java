package com.examsphere.attempt.DTOs;

public record AttemptQuestionResponse(
        Long questionId,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        Integer position
) {}