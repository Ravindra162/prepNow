package com.Assessment.AssessmentService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long companyId;
    private String name;
    private String description;
    private String domain;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssessmentDto> assessments;
}
