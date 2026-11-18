package com.examsphere.attempt.Entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "attempt_question_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attempt_question",
                columnNames = {"attempt_id", "question_id"}
        )
)
public class AttemptQuestionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false, length = 255)
    private String optionA;

    @Column(nullable = false, length = 255)
    private String optionB;

    @Column(nullable = false, length = 255)
    private String optionC;

    @Column(nullable = false, length = 255)
    private String optionD;

    @Column(nullable = false, length = 1)
    private String correctOption;

    @Column(nullable = false)
    private Integer marks;

    @Column(nullable = false)
    private Integer position;

    protected AttemptQuestionSnapshot() {}

    public AttemptQuestionSnapshot(
            Long attemptId,
            Long questionId,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption,
            Integer marks,
            Integer position) {

        this.attemptId = attemptId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.marks = marks;
        this.position = position;
    }

    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public Integer getMarks() { return marks; }
    public Integer getPosition() { return position; }
}