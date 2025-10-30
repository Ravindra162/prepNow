package com.Submission.SubmissionService.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "code_executions")
public class CodeExecution {
    @Id
    private String id;

    @Indexed
    private String submissionId;

    @Indexed
    private String fileId;

    @Indexed
    private String questionId;

    private String language;

    private Instant executedAt;
    private Long runTimeMs;
    private Long memoryUsedMb;
    private Integer exitCode;

    @Indexed
    private ExecutionStatus status;

    private String stdout;
    private String stderr;
    private String compileOutput;

    @Builder.Default
    private List<Map<String, Object>> testCaseResults = new ArrayList<>();

    private Integer passedCount;
    private Integer totalCount;
    private Double score;

    private String artifactPath;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}

