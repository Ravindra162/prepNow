package com.Submission.SubmissionService.consumer;

import com.Submission.SubmissionService.config.RabbitMQConfig;
import com.Submission.SubmissionService.dto.EvaluationRequestMessage;
import com.Submission.SubmissionService.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvaluationConsumer {

    private final EvaluationService evaluationService;

    @RabbitListener(queues = RabbitMQConfig.EVALUATION_QUEUE)
    public void consumeEvaluationRequest(EvaluationRequestMessage message) {
        log.info("========================================");
        log.info("Received evaluation request from RabbitMQ");
        log.info("  - Submission ID: {}", message.getSubmissionId());
        log.info("  - User ID: {}", message.getUserId());
        log.info("  - Test ID: {}", message.getTestId());
        log.info("  - Requested At: {}", message.getRequestedAt());
        log.info("========================================");

        try {
            // Trigger automatic evaluation
            evaluationService.evaluateSubmission(message.getSubmissionId(), null);
            log.info("✓ Successfully completed automatic evaluation for submission: {}", message.getSubmissionId());
        } catch (Exception e) {
            log.error("❌ Error during automatic evaluation for submission {}: {}",
                    message.getSubmissionId(), e.getMessage(), e);

            // Log the error but don't throw it back to the queue to avoid infinite retries
            // In production, you might want to implement a dead letter queue or retry logic
            log.error("Evaluation failed and will not be retried. Manual intervention may be required.");
        }
    }
}
