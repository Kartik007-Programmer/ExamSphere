package com.examsphere.question.Repository;

import com.examsphere.question.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findByTopicIgnoreCase(String topic);

    List<Question> findByCreatedById(Long createdById);
}
