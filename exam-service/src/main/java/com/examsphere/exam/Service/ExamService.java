package com.examsphere.exam.Service;

import com.examsphere.exam.DTOs.CreateExamRequest;
import com.examsphere.exam.DTOs.ExamResponse;
import com.examsphere.exam.DTOs. UpdateExamRequest;
import com.examsphere.exam.Entity.Exam;
import com.examsphere.exam.Entity.ExamStatus;
import com.examsphere.exam.Repository.ExamRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionClient questionClient;
    private final Clock clock;

    public ExamService(
            ExamRepository examRepository,
            QuestionClient questionClient,
            Clock clock) {
        this.examRepository = examRepository;
        this.questionClient = questionClient;
        this.clock = clock;
    }

    @Transactional
    public ExamResponse create(
            CreateExamRequest request,
            Long createdById,
            String accessToken) {

        validateSchedule(
                request.startTime(),
                request.endTime());

        validateQuestionIds(request.questionIds());

        questionClient.validateQuestions(
                request.questionIds(),
                accessToken);

        Exam exam = new Exam(
                request.title().trim(),
                request.description(),
                request.durationMinutes(),
                request.totalMarks(),
                request.startTime(),
                request.endTime(),
                createdById,
                request.questionIds()
        );

        return toResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public ExamResponse getById(
            Long id,
            Long currentUserId,
            boolean isAdmin) {

        Exam exam = findExam(id);

        verifyOwnership(exam, currentUserId, isAdmin);

        return toResponse(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getMyExams(Long currentUserId) {

        return examRepository
                .findByCreatedById(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getPublishedExams() {

        return examRepository
                .findByStatus(ExamStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExamResponse update(
            Long id,
            UpdateExamRequest request,
            Long currentUserId,
            boolean isAdmin,
            String accessToken) {

        Exam exam = findExam(id);

        verifyOwnership(exam, currentUserId, isAdmin);

        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only draft exams can be updated");
        }

        validateSchedule(
                request.startTime(),
                request.endTime());

        validateQuestionIds(request.questionIds());

        questionClient.validateQuestions(
                request.questionIds(),
                accessToken);

        exam.update(
                request.title().trim(),
                request.description(),
                request.durationMinutes(),
                request.totalMarks(),
                request.startTime(),
                request.endTime(),
                request.questionIds()
        );

        return toResponse(exam);
    }

    @Transactional
    public ExamResponse publish(
            Long id,
            Long currentUserId,
            boolean isAdmin) {

        Exam exam = findExam(id);

        verifyOwnership(exam, currentUserId, isAdmin);

        try {
            exam.publish();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ex.getMessage());
        }

        return toResponse(exam);
    }

    @Transactional
    public void cancel(
            Long id,
            Long currentUserId,
            boolean isAdmin) {

        Exam exam = findExam(id);

        verifyOwnership(exam, currentUserId, isAdmin);

        exam.cancel();
    }

    @Transactional(readOnly = true)
    public ExamResponse getForAttempt(Long id) {

        Exam exam = findExam(id);

        Instant now = Instant.now(clock);

        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            System.out.println("Exam is not published");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam is not published");
        }

        if (now.isBefore(exam.getStartTime()) ||
                !now.isBefore(exam.getEndTime())) {
            System.out.println("Exam is not currently available. Exam Start Time : "
                    +exam.getStartTime()+" End Time : "+exam.getEndTime());
            System.out.println("Current Time : "+now);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam is not currently available");
        }
        System.out.println("Current Time : "+now);

        return toResponse(exam);
    }

    private Exam findExam(Long id) {

        return examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam not found"));
    }

    private void verifyOwnership(
            Exam exam,
            Long currentUserId,
            boolean isAdmin) {

        if (!isAdmin &&
                !exam.getCreatedById().equals(currentUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot manage another teacher's exam");
        }
    }

    private void validateSchedule(
            Instant startTime,
            Instant endTime) {

        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exam end time must be after start time");
        }
    }

    private void validateQuestionIds(List<Long> questionIds) {

        if (questionIds.size() != new HashSet<>(questionIds).size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate question IDs are not allowed");
        }
    }

    private ExamResponse toResponse(Exam exam) {

        return new ExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getDurationMinutes(),
                exam.getTotalMarks(),
                exam.getStartTime(),
                exam.getEndTime(),
                exam.getCreatedById(),
                exam.getStatus(),
                exam.getQuestionIds(),
                exam.getCreatedAt()
        );
    }
}