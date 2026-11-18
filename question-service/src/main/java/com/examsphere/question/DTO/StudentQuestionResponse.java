package com.examsphere.question.DTO;

import com.examsphere.question.Entity.Difficulty;

import java.io.Serializable;

public record StudentQuestionResponse(
        Long id,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        Difficulty difficulty
) implements Serializable {
}