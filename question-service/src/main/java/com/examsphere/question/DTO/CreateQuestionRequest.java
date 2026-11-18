package com.examsphere.question.DTO;

import com.examsphere.question.Entity.CorrectOption;
import com.examsphere.question.Entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotBlank @Size(max = 1000) String questionText,
        @NotBlank @Size(max = 255) String optionA,
        @NotBlank @Size(max = 255) String optionB,
        @NotBlank @Size(max = 255) String optionC,
        @NotBlank @Size(max = 255) String optionD,
        @NotNull CorrectOption correctOption,
        @NotBlank @Size(max = 100) String topic,
        @NotNull Difficulty difficulty
) {}