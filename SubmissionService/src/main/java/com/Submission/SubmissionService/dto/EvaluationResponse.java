package com.Submission.SubmissionService.dto;

import com.Submission.SubmissionService.domain.QuestionResult;
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
public class EvaluationResponse {
    private String id;
    private String submissionId;

    // Overall scores
    private Double totalScore;
    private Double maxScore;
    private Double percentageScore;

    // MCQ scores
    private Double mcqScore;
    private Double mcqMaxScore;
    private Integer mcqCorrect;
    private Integer mcqTotal;

    // Coding scores
    private Double codingScore;
    private Double codingMaxScore;
    private Integer codingPassed;
    private Integer codingTotal;

    private Instant evaluatedAt;
    private String evaluatorId;
    private String remarks;

    private Map<String, Double> breakdown;
    private Map<String, Object> detailedResults;

    // Question-level results
    private List<QuestionResult> questionResults;

    private Boolean passed;
    private Double passingThreshold;

    // Statistics
    private Integer totalQuestionsAttempted;
    private Integer totalQuestionsCorrect;
    private Integer totalQuestionsIncorrect;
    private Integer totalQuestionsUnanswered;
}
