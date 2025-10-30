package com.Assessment.AssessmentService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScoreRequest {
    private Long assessmentCandidateId;
    private Double totalScore;
    private Double maxScore;
    private Double percentageScore;
    private Boolean isPassed;
    private Integer totalQuestions;
    private Integer attemptedQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer unansweredQuestions;
    private Integer mcqCorrect;
    private Integer mcqTotal;
    private Integer codingPassed;
    private Integer codingTotal;
}

