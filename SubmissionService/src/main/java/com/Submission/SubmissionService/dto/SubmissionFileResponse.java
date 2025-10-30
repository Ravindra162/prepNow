package com.Submission.SubmissionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFileResponse {
    private String id;
    private String submissionId;
    private String questionId;
    private String filename;
    private String language;
    private String storagePath;
    private Long sizeBytes;
    private Instant uploadedAt;
    private String content;
}

