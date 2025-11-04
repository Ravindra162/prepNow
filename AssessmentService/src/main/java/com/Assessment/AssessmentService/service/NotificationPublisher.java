package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.config.RabbitMQConfig;
import com.Assessment.AssessmentService.dto.NotificationMessage;
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
                                                   String assessmentName, String companyName, Integer durationMinutes) {
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

        publishNotification(RabbitMQConfig.ASSESSMENT_STARTED_KEY, message);
    }

    /**
     * Send assessment submitted notification
     */
    public void sendAssessmentSubmittedNotification(Integer userId, String userEmail, String userName,
                                                     String assessmentName, String companyName, Integer timeTakenMinutes) {
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

        publishNotification(RabbitMQConfig.ASSESSMENT_SUBMITTED_KEY, message);
    }

    /**
     * Send assessment reminder notification
     */
    public void sendAssessmentReminderNotification(Integer userId, String userEmail, String userName,
                                                    String assessmentName, String companyName) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("reminderSentAt", LocalDateTime.now().toString());

        NotificationMessage message = NotificationMessage.builder()
                .notificationType("ASSESSMENT_REMINDER")
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .assessmentName(assessmentName)
                .companyName(companyName)
                .additionalData(additionalData)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(RabbitMQConfig.ASSESSMENT_REMINDER_KEY, message);
    }

    /**
     * Publish notification message to RabbitMQ
     */
    private void publishNotification(String routingKey, NotificationMessage message) {
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
