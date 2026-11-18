package com.examsphere.attempt.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SaveAnswerRequest(

        @NotBlank
        @Pattern(regexp = "[ABCD]")
        String selectedOption
) {}