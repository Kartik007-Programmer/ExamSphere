package com.examsphere.attempt.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "exam_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attempt_exam_student",
                columnNames = {"exam_id", "student_id"}
        ),
        indexes = @Index(
                name = "idx_attempts_expiry",
                columnList = "status, expires_at, id"
        )
)
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptStatus status;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Integer obtainedMarks = 0;

    private Double percentage;

    protected ExamAttempt() {}

    public ExamAttempt(
            Long examId,
            Long studentId,
            Instant startedAt,
            Instant expiresAt,
            Integer totalMarks) {

        this.examId = examId;
        this.studentId = studentId;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.totalMarks = totalMarks;
        this.status = AttemptStatus.IN_PROGRESS;
        this.obtainedMarks = 0;
    }

    public void submit(
            Instant submittedAt,
            int obtainedMarks,
            boolean automatic) {

        if (this.status != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Attempt has already been submitted");
        }

        this.submittedAt = submittedAt;
        this.obtainedMarks = obtainedMarks;
        this.percentage = totalMarks == 0
                ? 0.0
                : obtainedMarks * 100.0 / totalMarks;

        this.status = automatic
                ? AttemptStatus.AUTO_SUBMITTED
                : AttemptStatus.SUBMITTED;
    }

    public Long getId() { return id; }
    public Long getExamId() { return examId; }
    public Long getStudentId() { return studentId; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public AttemptStatus getStatus() { return status; }
    public Integer getTotalMarks() { return totalMarks; }
    public Integer getObtainedMarks() { return obtainedMarks; }
    public Double getPercentage() { return percentage; }
}