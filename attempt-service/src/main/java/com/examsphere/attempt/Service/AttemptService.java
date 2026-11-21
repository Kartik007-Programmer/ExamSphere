package com.examsphere.attempt.Service;

import com.examsphere.attempt.DTOs.*;
import com.examsphere.attempt.Entity.*;
import com.examsphere.attempt.Repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttemptService {

    private final ExamAttemptRepository attemptRepository;
    private final AttemptAnswerRepository answerRepository;
    private final AttemptQuestionSnapshotRepository snapshotRepository;
    private final ExamClient examClient;
    private final QuestionClient questionClient;
    private final Clock clock;

    public AttemptService(
            ExamAttemptRepository attemptRepository,
            AttemptAnswerRepository answerRepository,
            AttemptQuestionSnapshotRepository snapshotRepository,
            ExamClient examClient,
            QuestionClient questionClient,
            Clock clock) {

        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.snapshotRepository = snapshotRepository;
        this.examClient = examClient;
        this.questionClient = questionClient;
        this.clock = clock;
    }

    @Transactional
    public StartAttemptResponse startAttempt(
            Long examId,
            Long studentId,
            String accessToken) {

        if (attemptRepository
                .findByExamIdAndStudentId(examId, studentId)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already attempted this exam");
        }

        ExamClient.ExamInfo exam =
                examClient.getExamForAttempt(examId, accessToken);

        Instant now = Instant.now(clock);

        Instant durationExpiry =
                now.plusSeconds(exam.durationMinutes() * 60L);

        Instant expiresAt = durationExpiry.isBefore(exam.endTime())
                ? durationExpiry
                : exam.endTime();

        ExamAttempt attempt = new ExamAttempt(
                examId,
                studentId,
                now,
                expiresAt,
                exam.totalMarks()
        );

        ExamAttempt savedAttempt =
                attemptRepository.save(attempt);

        List<Long> questionIds = exam.questionIds();

        if (questionIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This exam has no questions");
        }

        int questionCount = questionIds.size();

        // Distribute whole-number marks across questions.
        int baseMarks = exam.totalMarks() / questionCount;
        int remainder = exam.totalMarks() % questionCount;

        List<AttemptQuestionSnapshot> snapshots =
                new ArrayList<>();

        for (int i = 0; i < questionCount; i++) {

            Long questionId = questionIds.get(i);

            QuestionClient.QuestionInfo question =
                    questionClient.getQuestionSnapshot(questionId);

            if (question == null ||
                    question.correctOption() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Question data is incomplete");
            }

            int marks = baseMarks + (i < remainder ? 1 : 0);

            AttemptQuestionSnapshot snapshot =
                    new AttemptQuestionSnapshot(
                            savedAttempt.getId(),
                            question.id(),
                            question.questionText(),
                            question.optionA(),
                            question.optionB(),
                            question.optionC(),
                            question.optionD(),
                            question.correctOption(),
                            marks,
                            i + 1
                    );

            snapshots.add(snapshot);
        }

        snapshotRepository.saveAll(snapshots);

        return toStartResponse(savedAttempt, exam.title(), snapshots);
    }

    @Transactional
    public Optional<SubmitAttemptResponse> saveAnswer(
            Long attemptId,
            Long studentId,
            Long questionId,
            SaveAnswerRequest request) {

        ExamAttempt attempt = findOwnedAttempt(attemptId, studentId);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This attempt is already submitted");
        }

        if (!Instant.now(clock).isBefore(attempt.getExpiresAt())) {
            SubmitAttemptResponse submitted = submitInternally(attempt, true);
            return Optional.of(submitted);
        }

        snapshotRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question does not belong to this attempt"));

        AttemptAnswer answer = answerRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId)
                .orElseGet(() -> new AttemptAnswer(
                        attemptId, questionId, request.selectedOption()));

        answer.updateAnswer(request.selectedOption());
        answerRepository.save(answer);
        return Optional.empty();
    }

    @Transactional
    public SubmitAttemptResponse submitAttempt(
            Long attemptId,
            Long studentId) {

        ExamAttempt attempt = findOwnedAttempt(attemptId, studentId);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attempt has already been submitted");
        }

        boolean automatic =
                !Instant.now(clock).isBefore(attempt.getExpiresAt());

        return submitInternally(attempt, automatic);
    }

    private SubmitAttemptResponse submitInternally(
            ExamAttempt attempt,
            boolean automatic) {

        List<AttemptQuestionSnapshot> snapshots =
                snapshotRepository
                        .findByAttemptIdOrderByPosition(attempt.getId());

        List<AttemptAnswer> answers =
                answerRepository.findByAttemptId(attempt.getId());

        int obtainedMarks = 0;

        for (AttemptQuestionSnapshot snapshot : snapshots) {

            AttemptAnswer answer = answers.stream()
                    .filter(a -> a.getQuestionId()
                            .equals(snapshot.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (answer == null) {
                continue;
            }

            boolean correct = snapshot.getCorrectOption()
                    .equals(answer.getSelectedOption());

            int marks = correct ? snapshot.getMarks() : 0;

            answer.evaluate(correct, marks);

            obtainedMarks += marks;
        }

        answerRepository.saveAll(answers);

        attempt.submit(
                Instant.now(clock),
                obtainedMarks,
                automatic
        );

        ExamAttempt savedAttempt =
                attemptRepository.save(attempt);

        return new SubmitAttemptResponse(
                savedAttempt.getId(),
                savedAttempt.getStatus().name(),
                savedAttempt.getTotalMarks(),
                savedAttempt.getObtainedMarks(),
                savedAttempt.getPercentage()
        );
    }

    @Transactional(readOnly = true)
    public ResultResponse getResult(
            Long attemptId,
            Long studentId) {

        ExamAttempt attempt = findOwnedAttempt(attemptId, studentId);

        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Result is not available until submission");
        }

        return new ResultResponse(
                attempt.getId(),
                attempt.getExamId(),
                attempt.getStatus().name(),
                attempt.getTotalMarks(),
                attempt.getObtainedMarks(),
                attempt.getPercentage(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt()
        );
    }

    private ExamAttempt findOwnedAttempt(
            Long attemptId,
            Long studentId) {

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attempt not found"));

        if (!attempt.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another student's attempt");
        }

        return attempt;
    }

    private StartAttemptResponse toStartResponse(
            ExamAttempt attempt,
            String examTitle,
            List<AttemptQuestionSnapshot> snapshots) {

        List<AttemptQuestionResponse> questions =
                snapshots.stream()
                        .map(q -> new AttemptQuestionResponse(
                                q.getQuestionId(),
                                q.getQuestionText(),
                                q.getOptionA(),
                                q.getOptionB(),
                                q.getOptionC(),
                                q.getOptionD(),
                                q.getPosition()
                        ))
                        .toList();

        return new StartAttemptResponse(
                attempt.getId(),
                attempt.getExamId(),
                examTitle,
                attempt.getStartedAt(),
                attempt.getExpiresAt(),
                questions
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void autoSubmitExpiredAttempt(Long attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Attempt not found")
                );

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return;
        }

        if (Instant.now(clock).isBefore(attempt.getExpiresAt())) {
            return;
        }

        submitInternally(attempt, true);
    }
}