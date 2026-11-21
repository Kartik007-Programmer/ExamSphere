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

    @GetMapping("/PING")
    public String ping() {
        return "<h1 style=\"\n" +
                "  margin: 0;\n" +
                "  min-height: 100vh;\n" +
                "  display: flex;\n" +
                "  align-items: center;\n" +
                "  justify-content: center;\n" +
                "  text-align: center;\n" +
                "  padding: 24px;\n" +
                "  box-sizing: border-box;\n" +
                "  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                "  font-size: clamp(2rem, 5vw, 3.5rem);\n" +
                "  font-weight: 800;\n" +
                "  letter-spacing: -0.02em;\n" +
                "  line-height: 1.2;\n" +
                "  color: #1e293b;\n" +
                "  background: radial-gradient(circle at top, #ecfdf5 0%, #f8fafc 60%, #e2e8f0 100%);\n" +
                "  text-shadow: 0 4px 18px rgba(5, 150, 105, 0.18);\n" +
                "\">\n" +
                "  ✍\uFE0F This is Attempt Service...\n" +
                "</h1>";
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
    public ResponseEntity<?> saveAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId,
            @Valid @RequestBody SaveAnswerRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return attemptService
                .saveAnswer(attemptId, currentStudentId(jwt), questionId, request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
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