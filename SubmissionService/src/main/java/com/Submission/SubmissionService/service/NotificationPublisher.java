package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.config.RabbitMQConfig;
import com.Submission.SubmissionService.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        additionalData.put("startedAt", LocalDateTime.now().toString());

        NotificationMessage message = NotificationMessage.builder()
                .notificationType("ASSESSMENT_STARTED")
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .assessmentName(assessmentName)
                .companyName(companyName)
                .additionalData(additionalData)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(message, RabbitMQConfig.ASSESSMENT_STARTED_KEY);
    }

    /**
     * Send assessment submitted notification
     */
    public void sendAssessmentSubmittedNotification(Integer userId, String userEmail, String userName,
                                                     String assessmentName, String companyName,
                                                     Integer timeTakenMinutes) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("timeTakenMinutes", timeTakenMinutes);
        additionalData.put("submittedAt", LocalDateTime.now().toString());

        NotificationMessage message = NotificationMessage.builder()
                .notificationType("ASSESSMENT_SUBMITTED")
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .assessmentName(assessmentName)
                .companyName(companyName)
                .additionalData(additionalData)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(message, RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY);
    }

    /**
     * Send evaluation completed notification
     */
    public void sendEvaluationCompletedNotification(Integer userId, String userEmail, String userName,
                                                     String assessmentName, String companyName,
                                                     Double totalScore, Double maxScore,
                                                     Double percentage, Boolean isPassed) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("score", totalScore);
        additionalData.put("maxScore", maxScore);
        additionalData.put("percentage", percentage);
        additionalData.put("isPassed", isPassed);
        additionalData.put("evaluatedAt", LocalDateTime.now().toString());

        NotificationMessage message = NotificationMessage.builder()
                .notificationType("ASSESSMENT_EVALUATED")
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .assessmentName(assessmentName)
                .companyName(companyName)
                .additionalData(additionalData)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(message, RabbitMQConfig.ASSESSMENT_EVALUATED_KEY);
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
