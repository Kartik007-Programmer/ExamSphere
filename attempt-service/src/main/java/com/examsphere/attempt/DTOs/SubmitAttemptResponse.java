package com.examsphere.attempt.DTOs;

public record SubmitAttemptResponse(
        Long attemptId,
        String status,
        Integer totalMarks,
        Integer obtainedMarks,
        Double percentage
) {}
