package com.Submission.SubmissionService.dto;

import com.Submission.SubmissionService.domain.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private String id;
    private String userId;
    private String testId;
    private Instant createdAt;
    private SubmissionStatus status;
    private Double totalScore;
    private Double maxScore;
    private Map<String, Object> metadata;
    private String evaluationId;
}
