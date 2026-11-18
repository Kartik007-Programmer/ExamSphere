package com.examsphere.attempt.DTOs;

import java.time.Instant;

public record ResultResponse(
        Long attemptId,
        Long examId,
        String status,
        Integer totalMarks,
        Integer obtainedMarks,
        Double percentage,
        Instant startedAt,
        Instant submittedAt
) {}