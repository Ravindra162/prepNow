package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private String notificationType; // ASSESSMENT_STARTED, ASSESSMENT_SUBMITTED, ASSESSMENT_EVALUATED, ASSESSMENT_REMINDER
    private Integer userId;
    private String userEmail;
    private String userName;
    private String assessmentName;
    private String companyName;
    private Map<String, Object> additionalData;
    private LocalDateTime timestamp;
}
