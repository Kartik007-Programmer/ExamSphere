package com.examsphere.attempt.Repository;

import com.examsphere.attempt.Entity.AttemptQuestionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptQuestionSnapshotRepository
        extends JpaRepository<AttemptQuestionSnapshot, Long> {

    List<AttemptQuestionSnapshot> findByAttemptIdOrderByPosition(
            Long attemptId);

    Optional<AttemptQuestionSnapshot> findByAttemptIdAndQuestionId(
            Long attemptId, Long questionId);
}