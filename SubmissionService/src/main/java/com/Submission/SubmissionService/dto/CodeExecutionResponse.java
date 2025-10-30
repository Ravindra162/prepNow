package com.Submission.SubmissionService.dto;

import com.Submission.SubmissionService.domain.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {
    private String id;
    private String submissionId;
    private String fileId;
    private String questionId;
    private String language;
    private Instant executedAt;
    private Long runTimeMs;
    private Long memoryUsedMb;
    private Integer exitCode;
    private ExecutionStatus status;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private List<Map<String, Object>> testCaseResults;
    private Integer passedCount;
    private Integer totalCount;
    private Double score;
    private String artifactPath;
    private Map<String, Object> metadata;
}

