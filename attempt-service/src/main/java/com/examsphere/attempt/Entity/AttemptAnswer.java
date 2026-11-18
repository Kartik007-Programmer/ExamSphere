package com.examsphere.attempt.Entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "attempt_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attempt_answer",
                columnNames = {"attempt_id", "question_id"}
        )
)
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long attemptId;

    @Column(nullable = false)
    private Long questionId;

    @Column(length = 1)
    private String selectedOption;

    @Column(nullable = false)
    private Boolean isCorrect = false;

    @Column(nullable = false)
    private Integer marksObtained = 0;

    protected AttemptAnswer() {}

    public AttemptAnswer(
            Long attemptId,
            Long questionId,
            String selectedOption) {

        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    public void updateAnswer(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void evaluate(
            boolean isCorrect,
            int marksObtained) {

        this.isCorrect = isCorrect;
        this.marksObtained = marksObtained;
    }

    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public Long getQuestionId() { return questionId; }
    public String getSelectedOption() { return selectedOption; }
    public Boolean getIsCorrect() { return isCorrect; }
    public Integer getMarksObtained() { return marksObtained; }
}