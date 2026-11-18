package com.examsphere.question.DTO;

import com.examsphere.question.Entity.CorrectOption;
import com.examsphere.question.Entity.Difficulty;
import java.time.Instant;

public record QuestionResponse(
        Long id,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        CorrectOption correctOption,
        String topic,
        Difficulty difficulty,
        Long createdById,
        Instant createdAt
) {}