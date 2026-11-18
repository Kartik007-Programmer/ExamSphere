package com.examsphere.attempt.Controller;

import com.examsphere.attempt.DTOs.*;
import com.examsphere.attempt.Service.AttemptService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/exams/{examId}/start")
    public ResponseEntity<StartAttemptResponse> startAttempt(
            @PathVariable Long examId,
            @AuthenticationPrincipal Jwt jwt) {

        StartAttemptResponse response =
                attemptService.startAttempt(
                        examId,
                        currentStudentId(jwt),
                        jwt.getTokenValue()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{attemptId}/questions/{questionId}/answer")
    public ResponseEntity<Void> saveAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId,
            @Valid @RequestBody SaveAnswerRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        attemptService.saveAnswer(
                attemptId,
                currentStudentId(jwt),
                questionId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{attemptId}/submit")
    public SubmitAttemptResponse submitAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal Jwt jwt) {

        return attemptService.submitAttempt(
                attemptId,
                currentStudentId(jwt)
        );
    }

    @GetMapping("/{attemptId}/result")
    public ResultResponse getResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal Jwt jwt) {

        return attemptService.getResult(
                attemptId,
                currentStudentId(jwt)
        );
    }

    private Long currentStudentId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}