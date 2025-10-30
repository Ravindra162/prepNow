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
@Document(collection = "submissions")
public class Submission {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String testId;

    private Instant createdAt;
    private Instant submittedAt;

    @Indexed
    private SubmissionStatus status;

    private Double totalScore;
    private Double maxScore;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Builder.Default
    private List<String> fileIds = new ArrayList<>();

    private String evaluationId;
}

