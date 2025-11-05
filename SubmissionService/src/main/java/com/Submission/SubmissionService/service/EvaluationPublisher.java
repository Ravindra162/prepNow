package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.config.RabbitMQConfig;
import com.Submission.SubmissionService.dto.EvaluationRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish evaluation request to RabbitMQ queue for automatic processing
     */
    public void publishEvaluationRequest(String submissionId, String userId, String testId) {
        try {
            EvaluationRequestMessage message = EvaluationRequestMessage.builder()
                    .submissionId(submissionId)
                    .userId(userId)
                    .testId(testId)
                    .requestedAt(Instant.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EVALUATION_EXCHANGE,
                    RabbitMQConfig.EVALUATION_REQUEST_KEY,
                    message
            );

            log.info("Published evaluation request to queue for submission: {}", submissionId);
        } catch (Exception e) {
            log.error("Failed to publish evaluation request for submission {}: {}", submissionId, e.getMessage(), e);
            throw new RuntimeException("Failed to publish evaluation request", e);
        }
    }
}
