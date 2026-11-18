package com.examsphere.exam.DTOs;

import com.examsphere.exam.Entity.ExamStatus;
import java.time.Instant;
import java.util.List;

public record ExamResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        Integer totalMarks,
        Instant startTime,
        Instant endTime,
        Long createdById,
        ExamStatus status,
        List<Long> questionIds,
        Instant createdAt
) {}