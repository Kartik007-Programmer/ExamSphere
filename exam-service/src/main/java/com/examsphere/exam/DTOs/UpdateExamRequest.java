package com.examsphere.exam.DTOs;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public record UpdateExamRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 1000)
        String description,

        @NotNull
        @Min(1)
        @Max(600)
        Integer durationMinutes,

        @NotNull
        @Min(1)
        Integer totalMarks,

        @NotNull
        Instant startTime,

        @NotNull
        Instant endTime,

        @NotEmpty
        @Size(max = 200)
        List<@NotNull @Positive Long> questionIds
) {}
