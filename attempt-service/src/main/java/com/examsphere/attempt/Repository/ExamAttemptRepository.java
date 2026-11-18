package com.examsphere.attempt.Repository;

import com.examsphere.attempt.Entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamAttemptRepository
        extends JpaRepository<ExamAttempt, Long> {

    Optional<ExamAttempt> findByExamIdAndStudentId(
            Long examId, Long studentId);
}