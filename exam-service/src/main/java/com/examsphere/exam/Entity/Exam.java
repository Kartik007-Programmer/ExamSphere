package com.examsphere.exam.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false, updatable = false)
    private Long createdById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status;

    @ElementCollection
    @CollectionTable(
            name = "exam_questions",
            joinColumns = @JoinColumn(name = "exam_id")
    )
    @OrderColumn(name = "question_position")
    @Column(name = "question_id", nullable = false)
    private List<Long> questionIds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Exam() {
        // Required by JPA
    }

    public Exam(
            String title,
            String description,
            Integer durationMinutes,
            Integer totalMarks,
            Instant startTime,
            Instant endTime,
            Long createdById,
            List<Long> questionIds) {

        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdById = createdById;
        this.questionIds = new ArrayList<>(questionIds);
        this.status = ExamStatus.DRAFT;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(
            String title,
            String description,
            Integer durationMinutes,
            Integer totalMarks,
            Instant startTime,
            Instant endTime,
            List<Long> questionIds) {

        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.startTime = startTime;
        this.endTime = endTime;

        this.questionIds = new ArrayList<>(questionIds);
    }

    public void publish() {
        if (this.status != ExamStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft exams can be published");
        }

        this.status = ExamStatus.PUBLISHED;
    }

    public void cancel() {
        if (this.status == ExamStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Exam is already cancelled");
        }

        this.status = ExamStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Integer getTotalMarks() { return totalMarks; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Long getCreatedById() { return createdById; }
    public ExamStatus getStatus() { return status; }
    public List<Long> getQuestionIds() {
        return List.copyOf(questionIds);
    }
    public Instant getCreatedAt() { return createdAt; }
}
