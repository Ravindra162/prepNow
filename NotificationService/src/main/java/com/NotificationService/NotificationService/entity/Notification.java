package com.NotificationService.NotificationService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String notificationType;
    private Integer userId;
    private String userEmail;
    private String userName;
    private String assessmentName;
    private String companyName;

    private String status; // PENDING, SENT, FAILED
    private String errorMessage;

    private Map<String, Object> additionalData;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}

