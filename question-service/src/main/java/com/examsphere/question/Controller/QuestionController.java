package com.examsphere.question.Controller;

import com.examsphere.question.DTO.CreateQuestionRequest;
import com.examsphere.question.DTO.QuestionResponse;
import com.examsphere.question.DTO.StudentQuestionResponse;
import com.examsphere.question.Service.QuestionService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;
    private final String internalServiceKey;

    public QuestionController(
            QuestionService service,
            @Value("${services.internal.key}") String internalServiceKey) {
        this.service = service;
        this.internalServiceKey = internalServiceKey;
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
                "  background: radial-gradient(circle at top, #ecfeff 0%, #f8fafc 60%, #e2e8f0 100%);\n" +
                "  text-shadow: 0 4px 18px rgba(8, 145, 178, 0.18);\n" +
                "\">\n" +
                "  ❓ This is Question Service...\n" +
                "</h1>";
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long createdById = Long.valueOf(jwt.getSubject());

        QuestionResponse response =
                service.create(request, createdById);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/multi")
    public ResponseEntity<List<QuestionResponse>> createMultiple(
            @Valid @RequestBody List<CreateQuestionRequest> request,
            @AuthenticationPrincipal Jwt jwt) {

        Long createdById = Long.valueOf(jwt.getSubject());

        List<QuestionResponse> response =
                service.createMultiple(request, createdById);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public QuestionResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/student/{id}")
    public StudentQuestionResponse getStudentQuestion(@PathVariable Long id) {
        return service.getStudentQuestionById(id);
    }

    @GetMapping
    public Page<QuestionResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }

        return service.getAll(PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    public QuestionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long createdById = Long.valueOf(jwt.getSubject());

        boolean isAdmin = "ADMIN".equals(
                jwt.getClaimAsString("role"));

        return service.update(id, request,createdById, isAdmin);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal Jwt jwt) {

        Long createdById = Long.valueOf(jwt.getSubject());

        boolean isAdmin = "ADMIN".equals(
                jwt.getClaimAsString("role"));

        service.delete(id, createdById, isAdmin);
    }

    @GetMapping("/internal/{id}/snapshot")
    public QuestionResponse getInternalSnapshot(
            @PathVariable Long id,
            @RequestHeader("X-Internal-Service-Key") String key) {

        boolean validKey = MessageDigest.isEqual(
                internalServiceKey.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8)
        );

        if (!validKey) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid internal service credential");
        }

        return service.getById(id);
    }
}