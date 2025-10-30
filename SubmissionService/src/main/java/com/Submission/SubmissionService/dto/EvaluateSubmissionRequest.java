package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateSubmissionRequest {
    private Double passingThreshold; // Optional: percentage required to pass (e.g., 60.0)
    private Boolean autoEvaluateCoding; // Whether to auto-evaluate coding questions
}

