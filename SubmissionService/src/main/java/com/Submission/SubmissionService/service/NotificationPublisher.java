package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.config.RabbitMQConfig;
import com.Submission.SubmissionService.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send assessment started notification
     */
    public void sendAssessmentStartedNotification(Integer userId, String userEmail, String userName,
                                                   String assessmentName, String companyName,
                                                   Integer durationMinutes) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("durationMinutes", durationMinutes);

        NotificationMessage message = NotificationMessage.builder()
                .notificationType("ASSESSMENT_STARTED")
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .assessmentName(assessmentName)
                .companyName(companyName)
                .additionalData(additionalData)
                .build();

        publishNotification(message, RabbitMQConfig.ASSESSMENT_STARTED_KEY);
    }

    /**
     * Send assessment submitted notification
     */
    public void sendAssessmentSubmittedNotification(Integer userId, String userEmail, String userName,
                                                     String assessmentName, String companyName,
                                                     Integer timeTakenMinutes) {
        try {
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("timeTakenMinutes", timeTakenMinutes);

            NotificationMessage message = NotificationMessage.builder()
                    .notificationType("ASSESSMENT_SUBMITTED")
                    .userId(userId)
                    .userEmail(userEmail)
                    .userName(userName)
                    .assessmentName(assessmentName)
                    .companyName(companyName)
                    .status("SUBMITTED")
                    .additionalData(additionalData)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY,
                    message
            );

            log.info("Published assessment submitted notification for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish notification for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to publish notification", e);
        }
    }

    /**
     * Send evaluation completed notification
     */
    public void sendEvaluationCompletedNotification(Integer userId, String userEmail, String userName,
                                                     String assessmentName, String companyName,
                                                     Double totalScore, Double maxScore) {
        try {
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("score", totalScore);
            additionalData.put("maxScore", maxScore);
            additionalData.put("percentage", maxScore > 0 ? (totalScore / maxScore) * 100 : 0);

            NotificationMessage message = NotificationMessage.builder()
                    .notificationType("ASSESSMENT_EVALUATED")
                    .userId(userId)
                    .userEmail(userEmail)
                    .userName(userName)
                    .assessmentName(assessmentName)
                    .companyName(companyName)
                    .status("EVALUATED")
                    .additionalData(additionalData)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.ASSESSMENT_EVALUATED_KEY,
                    message
            );

            log.info("Published assessment evaluated notification for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish evaluation notification for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Publish notification message to RabbitMQ
     */
    private void publishNotification(NotificationMessage message, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    message
            );
            log.info("Notification published to RabbitMQ: type={}, user={}, routingKey={}",
                    message.getNotificationType(), message.getUserEmail(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish notification to RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
