package com.examsphere.attempt.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class QuestionClient {

    private final RestClient restClient;
    private final String internalKey;

    public QuestionClient(
            RestClient.Builder builder,
            @Value("${services.question-service.url}") String baseUrl,
            @Value("${services.internal.key}") String internalKey) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();

        this.internalKey = internalKey;
    }

    public QuestionInfo getQuestionSnapshot(Long questionId) {

        return restClient.get()
                .uri("/api/questions/internal/{id}/snapshot", questionId)
                .header("X-Internal-Service-Key", internalKey)
                .retrieve()
                .body(QuestionInfo.class);
    }

    public record QuestionInfo(
            Long id,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption
    ) {}
}