package com.Submission.SubmissionService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    @NotBlank(message = "Submission ID is required")
    private String submissionId;

    @NotBlank(message = "File ID is required")
    private String fileId;

    @NotBlank(message = "Question ID is required")
    private String questionId;

    private List<Map<String, Object>> testCases;
    private Long timeoutMs;
    private Long memoryLimitMb;
}

