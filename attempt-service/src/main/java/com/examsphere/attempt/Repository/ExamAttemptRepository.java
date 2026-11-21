package com.examsphere.attempt.Repository;

import com.examsphere.attempt.Entity.AttemptStatus;
import com.examsphere.attempt.Entity.ExamAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository
        extends JpaRepository<ExamAttempt, Long> {

    Optional<ExamAttempt> findByExamIdAndStudentId(
            Long examId, Long studentId);

    List<ExamAttempt> findByStatusAndExpiresAtLessThanEqual(
            AttemptStatus status,
            Instant currentTime
    );

    @Query("""
        SELECT a FROM ExamAttempt a
        WHERE a.status = :status
          AND a.expiresAt <= :now
          AND a.id > :lastId
        ORDER BY a.id ASC
        """)
    List<ExamAttempt> findExpiredAfter(
            @Param("status") AttemptStatus status,
            @Param("now") Instant now,
            @Param("lastId") long lastId,
            Pageable pageable);
}