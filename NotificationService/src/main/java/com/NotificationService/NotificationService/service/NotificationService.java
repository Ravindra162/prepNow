package com.NotificationService.NotificationService.service;

import com.NotificationService.NotificationService.dto.NotificationMessage;
import com.NotificationService.NotificationService.entity.Notification;
import com.NotificationService.NotificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void processNotification(NotificationMessage message) {
        log.info("Processing notification: {} for user: {}", message.getNotificationType(), message.getUserId());

        // Save notification to database
        Notification notification = Notification.builder()
                .notificationType(message.getNotificationType())
                .userId(message.getUserId())
                .userEmail(message.getUserEmail())
                .userName(message.getUserName())
                .assessmentName(message.getAssessmentName())
                .companyName(message.getCompanyName())
                .additionalData(message.getAdditionalData())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Send email based on notification type
        try {
            switch (message.getNotificationType()) {
                case "ASSESSMENT_STARTED":
                    sendAssessmentStartedEmail(notification, message);
                    break;
                case "ASSESSMENT_SUBMITTED":
                    sendAssessmentSubmittedEmail(notification, message);
                    break;
                case "ASSESSMENT_EVALUATED":
                    sendAssessmentEvaluatedEmail(notification, message);
                    break;
                case "ASSESSMENT_REMINDER":
                    sendAssessmentReminderEmail(notification, message);
                    break;
                default:
                    log.warn("Unknown notification type: {}", message.getNotificationType());
            }

            // Update notification status to SENT
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);

            // Update notification status to FAILED
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    private void sendAssessmentStartedEmail(Notification notification, NotificationMessage message) {
        Integer durationMinutes = message.getAdditionalData() != null
            ? (Integer) message.getAdditionalData().get("durationMinutes")
            : null;

        String subject = "Assessment Started - " + message.getAssessmentName();
        String htmlContent = emailService.buildAssessmentStartedEmail(
            message.getUserName(),
            message.getAssessmentName(),
            message.getCompanyName(),
            durationMinutes
        );

        emailService.sendHtmlEmail(message.getUserEmail(), subject, htmlContent);
    }

    private void sendAssessmentSubmittedEmail(Notification notification, NotificationMessage message) {
        Integer timeTakenMinutes = message.getAdditionalData() != null
            ? (Integer) message.getAdditionalData().get("timeTakenMinutes")
            : null;

        String subject = "Assessment Submitted - " + message.getAssessmentName();
        String htmlContent = emailService.buildAssessmentSubmittedEmail(
            message.getUserName(),
            message.getAssessmentName(),
            message.getCompanyName(),
            timeTakenMinutes
        );

        emailService.sendHtmlEmail(message.getUserEmail(), subject, htmlContent);
    }

    private void sendAssessmentEvaluatedEmail(Notification notification, NotificationMessage message) {
        if (message.getAdditionalData() == null) {
            log.warn("No additional data for evaluation email");
            return;
        }

        Double score = message.getAdditionalData().get("score") != null
            ? ((Number) message.getAdditionalData().get("score")).doubleValue()
            : 0.0;
        Double percentage = message.getAdditionalData().get("percentage") != null
            ? ((Number) message.getAdditionalData().get("percentage")).doubleValue()
            : 0.0;
        Boolean isPassed = (Boolean) message.getAdditionalData().get("isPassed");

        String subject = "Assessment Evaluation Complete - " + message.getAssessmentName();
        String htmlContent = emailService.buildAssessmentEvaluatedEmail(
            message.getUserName(),
            message.getAssessmentName(),
            message.getCompanyName(),
            score,
            percentage,
            isPassed
        );

        emailService.sendHtmlEmail(message.getUserEmail(), subject, htmlContent);
    }

    private void sendAssessmentReminderEmail(Notification notification, NotificationMessage message) {
        String subject = "Reminder: Complete Your Assessment - " + message.getAssessmentName();
        String htmlContent = emailService.buildAssessmentReminderEmail(
            message.getUserName(),
            message.getAssessmentName(),
            message.getCompanyName()
        );

        emailService.sendHtmlEmail(message.getUserEmail(), subject, htmlContent);
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatus("FAILED");
    }
}

