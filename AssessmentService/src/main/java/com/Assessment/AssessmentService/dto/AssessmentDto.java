package com.Assessment.AssessmentService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {
    private Long assessmentId;
    private Long companyId;
    private String companyName;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private Map<String, Object> structure;
    private LocalDateTime createdAt;
}
