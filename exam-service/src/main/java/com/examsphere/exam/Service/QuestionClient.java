package com.examsphere.exam.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class QuestionClient {

    private final RestClient restClient;

    public QuestionClient(
            RestClient.Builder builder,
            @Value("${services.question-service.url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public void validateQuestions(
            List<Long> questionIds,
            String accessToken) {

        for (Long questionId : questionIds) {

            try {
                restClient.get()
                        .uri("/api/questions/{id}", questionId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken)
                        .retrieve()
                        .toBodilessEntity();

            } catch (RestClientResponseException ex) {

                if (ex.getStatusCode().value() == 404) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Question not found: " + questionId);
                }

                if (ex.getStatusCode().value() == 401 ||
                        ex.getStatusCode().value() == 403) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Unable to access question: " + questionId);
                }

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Question Service returned an error");
            }
        }
    }
}