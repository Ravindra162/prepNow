package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequestMessage implements Serializable {
    private String submissionId;
    private String userId;
    private String testId;
    private Instant requestedAt;
}
