package com.Submission.SubmissionService.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResult {
    private String questionId;
    private String questionType; // MCQ or CODING
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Double pointsAwarded;
    private Double maxPoints;
    private String feedback;
    private String difficulty;
}

