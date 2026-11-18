package com.examsphere.question.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectOption correctOption;

    @Column(nullable = false, length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false, updatable = false)
    private Long createdById;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Question() {
    }

    public Question(
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            CorrectOption correctOption,
            String topic,
            Difficulty difficulty,
            Long createdById) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.topic = topic;
        this.difficulty = difficulty;
        this.createdById = createdById;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            CorrectOption correctOption,
            String topic,
            Difficulty difficulty) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.topic = topic;
        this.difficulty = difficulty;
    }

    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public CorrectOption getCorrectOption() { return correctOption; }
    public String getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public Long getCreatedById() { return createdById; }
    public Instant getCreatedAt() { return createdAt; }
}