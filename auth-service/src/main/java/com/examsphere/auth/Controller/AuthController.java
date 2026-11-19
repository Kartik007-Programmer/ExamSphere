package com.examsphere.auth.Controller;

import com.examsphere.auth.DTOs.AuthResponse;
import com.examsphere.auth.DTOs.LoginRequest;
import com.examsphere.auth.DTOs.RegisterRequest;
import com.examsphere.auth.Service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
                "  background: radial-gradient(circle at top, #fffbeb 0%, #f8fafc 60%, #e2e8f0 100%);\n" +
                "  text-shadow: 0 4px 18px rgba(217, 119, 6, 0.18);\n" +
                "\">\n" +
                "  \uD83D\uDD10 This is Auth Service...\n" +
                "</h1>";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(
            @AuthenticationPrincipal Jwt jwt) {

        return new CurrentUserResponse(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role")
        );
    }

    public record CurrentUserResponse(
            Long userId,
            String email,
            String role
    ) {}
}