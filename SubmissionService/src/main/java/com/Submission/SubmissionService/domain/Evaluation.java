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
@Document(collection = "evaluations")
public class Evaluation {
    @Id
    private String id;

    @Indexed(unique = true)
    private String submissionId;

    private Double totalScore;
    private Double maxScore;
    private Double percentageScore;

    private Double mcqScore;
    private Double mcqMaxScore;
    private Integer mcqCorrect;
    private Integer mcqTotal;

    private Double codingScore;
    private Double codingMaxScore;
    private Integer codingPassed;
    private Integer codingTotal;

    private Instant evaluatedAt;
    private String evaluatorId;
    private String remarks;

    @Builder.Default
    private Map<String, Double> breakdown = new HashMap<>();

    @Builder.Default
    private Map<String, Object> detailedResults = new HashMap<>();

    // Question-level results
    @Builder.Default
    private List<QuestionResult> questionResults = new ArrayList<>();

    private Boolean passed;
    private Double passingThreshold;

    // Statistics
    private Integer totalQuestionsAttempted;
    private Integer totalQuestionsCorrect;
    private Integer totalQuestionsIncorrect;
    private Integer totalQuestionsUnanswered;
}
