package com.NotificationService.NotificationService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding while sending HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email due to unsupported encoding", e);
        }
    }

    public String buildAssessmentStartedEmail(String userName, String assessmentName, String companyName, Integer durationMinutes) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #4CAF50;">Assessment Started</h2>
                    <p>Dear %s,</p>
                    <p>You have started the assessment:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Assessment:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Duration:</strong> %d minutes</p>
                    </div>
                    <p>Good luck with your assessment!</p>
                    <p style="margin-top: 30px; font-size: 12px; color: #777;">
                        This is an automated message from PrepNow Platform. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, userName, assessmentName, companyName, durationMinutes);
    }

    public String buildAssessmentSubmittedEmail(String userName, String assessmentName, String companyName, Integer timeTakenMinutes) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #2196F3;">Assessment Submitted Successfully</h2>
                    <p>Dear %s,</p>
                    <p>Your assessment has been submitted successfully:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Assessment:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Time Taken:</strong> %d minutes</p>
                    </div>
                    <p>Your submission is now being evaluated. You will receive another email once the evaluation is complete.</p>
                    <p style="margin-top: 30px; font-size: 12px; color: #777;">
                        This is an automated message from PrepNow Platform. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, userName, assessmentName, companyName, timeTakenMinutes);
    }

    public String buildAssessmentEvaluatedEmail(String userName, String assessmentName, String companyName,
                                                Double score, Double percentage, Boolean isPassed) {
        String statusColor = Boolean.TRUE.equals(isPassed) ? "#4CAF50" : "#F44336";
        String statusText = Boolean.TRUE.equals(isPassed) ? "Passed" : "Not Passed";

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: %s;">Assessment Evaluation Complete</h2>
                    <p>Dear %s,</p>
                    <p>Your assessment has been evaluated:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Assessment:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Score:</strong> %.2f</p>
                        <p><strong>Percentage:</strong> %.2f%%</p>
                        <p><strong>Status:</strong> <span style="color: %s; font-weight: bold;">%s</span></p>
                    </div>
                    <p>You can view detailed results by logging into your PrepNow account.</p>
                    <p style="margin-top: 30px; font-size: 12px; color: #777;">
                        This is an automated message from PrepNow Platform. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, statusColor, userName, assessmentName, companyName, score, percentage, statusColor, statusText);
    }

    public String buildAssessmentReminderEmail(String userName, String assessmentName, String companyName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #FF9800;">Assessment Reminder</h2>
                    <p>Dear %s,</p>
                    <p>This is a reminder about the following assessment:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Assessment:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                    </div>
                    <p>Please complete this assessment at your earliest convenience.</p>
                    <p style="margin-top: 30px; font-size: 12px; color: #777;">
                        This is an automated message from PrepNow Platform. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """, userName, assessmentName, companyName);
    }
}
