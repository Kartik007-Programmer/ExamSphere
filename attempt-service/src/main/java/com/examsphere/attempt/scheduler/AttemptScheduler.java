package com.examsphere.attempt.scheduler;

import com.examsphere.attempt.Entity.AttemptStatus;
import com.examsphere.attempt.Entity.ExamAttempt;
import com.examsphere.attempt.Repository.ExamAttemptRepository;
import com.examsphere.attempt.Service.AttemptService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class AttemptScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttemptScheduler.class);

    private final ExamAttemptRepository attemptRepository;
    private final AttemptService attemptService;
    private final Clock clock;
    private final int batchSize;

    public AttemptScheduler(
            ExamAttemptRepository attemptRepository,
            AttemptService attemptService,
            Clock clock,
            @Value("${attempts.auto-submit.batch-size:200}") int batchSize) {

        this.attemptRepository = attemptRepository;
        this.attemptService = attemptService;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Scheduled(
            fixedDelayString = "${attempts.auto-submit.interval-ms:15000}",
            initialDelayString = "${attempts.auto-submit.initial-delay-ms:5000}")
    public void autoSubmitExpiredAttempts() {

        Instant now = Instant.now(clock);
        long lastId = 0L;
        int totalProcessed = 0;

        while (true) {
            List<ExamAttempt> batch = attemptRepository.findExpiredAfter(
                    AttemptStatus.IN_PROGRESS,
                    now,
                    lastId,
                    PageRequest.of(0, batchSize));

            if (batch.isEmpty()) {
                break;
            }

            for (ExamAttempt attempt : batch) {
                try {
                    attemptService.autoSubmitExpiredAttempt(attempt.getId());
                    totalProcessed++;
                } catch (Exception exception) {
                    log.error(
                            "Failed to auto-submit attempt ID: {}",
                            attempt.getId(),
                            exception);
                }
            }

            lastId = batch.get(batch.size() - 1).getId();

            if (batch.size() < batchSize) {
                break;
            }
        }

        if (totalProcessed > 0) {
            log.info("Auto-submitted {} expired attempt(s)", totalProcessed);
        }
    }
}