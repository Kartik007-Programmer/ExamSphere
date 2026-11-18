package com.examsphere.attempt.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ExamClient {

    private final RestClient restClient;

    public ExamClient(
            RestClient.Builder builder,
            @Value("${services.exam-service.url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public ExamInfo getExamForAttempt(Long examId, String accessToken) {

        try {
            return restClient.get()
                    .uri("/api/exams/{id}/attempt-info", examId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(ExamInfo.class);

        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "exam-service returned " + ex.getStatusCode()
                            + " for examId=" + examId
                            + ", body=" + ex.getResponseBodyAsString());
        }
    }

    public record ExamInfo(
            Long id,
            String title,
            Integer durationMinutes,
            Integer totalMarks,
            java.time.Instant startTime,
            java.time.Instant endTime,
            String status,
            java.util.List<Long> questionIds
    ) {}
}