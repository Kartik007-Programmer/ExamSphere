package com.examsphere.exam.Repository;

import com.examsphere.exam.Entity.Exam;
import com.examsphere.exam.Entity.ExamStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository
        extends JpaRepository<Exam, Long> {

    List<Exam> findByCreatedById(Long createdById);

    List<Exam> findByStatus(ExamStatus status);
}