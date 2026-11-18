package com.examsphere.exam.Controller;

import com.examsphere.exam.DTOs.CreateExamRequest;
import com.examsphere.exam.DTOs.ExamResponse;
import com.examsphere.exam.DTOs.UpdateExamRequest;
import com.examsphere.exam.Service.ExamService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    public ResponseEntity<ExamResponse> create(
            @Valid @RequestBody CreateExamRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        ExamResponse response = examService.create(
                request,
                currentUserId(jwt),
                jwt.getTokenValue()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public List<ExamResponse> getMyExams(
            @AuthenticationPrincipal Jwt jwt) {

        return examService.getMyExams(currentUserId(jwt));
    }

    @GetMapping("/published")
    public List<ExamResponse> getPublishedExams() {
        return examService.getPublishedExams();
    }

    @GetMapping("/{id}")
    public ExamResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return examService.getById(
                id,
                currentUserId(jwt),
                isAdmin(jwt)
        );
    }

    @PutMapping("/{id}")
    public ExamResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return examService.update(
                id,
                request,
                currentUserId(jwt),
                isAdmin(jwt),
                jwt.getTokenValue()
        );
    }

    @PatchMapping("/{id}/publish")
    public ExamResponse publish(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return examService.publish(
                id,
                currentUserId(jwt),
                isAdmin(jwt)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        examService.cancel(
                id,
                currentUserId(jwt),
                isAdmin(jwt)
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attempt-info")
    public ExamResponse getForAttempt(@PathVariable Long id) {
        return examService.getForAttempt(id);
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}