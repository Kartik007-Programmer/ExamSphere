package com.examsphere.attempt.DTOs;

import java.time.Instant;
import java.util.List;

public record StartAttemptResponse(
        Long attemptId,
        Long examId,
        String examTitle,
        Instant startedAt,
        Instant expiresAt,
        List<AttemptQuestionResponse> questions
) {}