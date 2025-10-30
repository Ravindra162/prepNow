package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.domain.*;
import com.Submission.SubmissionService.dto.EvaluateSubmissionRequest;
import com.Submission.SubmissionService.dto.EvaluationResponse;
import com.Submission.SubmissionService.repository.EvaluationRepository;
import com.Submission.SubmissionService.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final RestTemplate restTemplate;

    private static final String ASSESSMENT_SERVICE_URL = "http://localhost:8081";
    private static final String QUESTION_SERVICE_URL = "http://localhost:8082";

    /**
     * Evaluate a submission by comparing user answers with correct answers
     */
    public EvaluationResponse evaluateSubmission(String submissionId, EvaluateSubmissionRequest request) {
        log.info("========================================");
        log.info("STARTING EVALUATION FOR SUBMISSION: {}", submissionId);
        log.info("========================================");

        // 1. Fetch submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        log.info("Submission Details:");
        log.info("  - User ID: {}", submission.getUserId());
        log.info("  - Test ID: {}", submission.getTestId());
        log.info("  - Created At: {}", submission.getCreatedAt());

        // Check if already evaluated
        Optional<Evaluation> existingEval = evaluationRepository.findBySubmissionId(submissionId);
        if (existingEval.isPresent()) {
            log.info("⚠️  Submission already evaluated: {}", submissionId);
            return mapToResponse(existingEval.get());
        }

        // 2. Get assessment structure and questions from metadata
        Map<String, Object> metadata = submission.getMetadata();
        @SuppressWarnings("unchecked")
        Map<String, Object> answers = (Map<String, Object>) metadata.get("answers");

        if (answers == null || answers.isEmpty()) {
            log.error("❌ No answers found in submission");
            throw new RuntimeException("No answers found in submission");
        }

        log.info("Total answers submitted: {}", answers.size());
        String testId = submission.getTestId();

        // 3. Fetch assessment structure
        log.info("Fetching assessment structure for testId: {}", testId);
        Map<String, Object> assessmentStructure = fetchAssessmentStructure(testId);
        log.info("✓ Assessment structure fetched successfully");

        // Debug: Log the structure to see what we received
        log.debug("Assessment structure keys: {}", assessmentStructure.keySet());
        if (assessmentStructure.containsKey("questionsMap")) {
            Object questionsMapObj = assessmentStructure.get("questionsMap");
            log.info("questionsMap type: {}", questionsMapObj != null ? questionsMapObj.getClass().getName() : "null");
            if (questionsMapObj instanceof Map) {
                Map<?, ?> qMap = (Map<?, ?>) questionsMapObj;
                log.info("questionsMap size: {}", qMap.size());
                log.info("questionsMap keys: {}", qMap.keySet());
            }
        } else {
            log.error("❌ questionsMap key not found in assessment structure!");
            log.error("Available keys: {}", assessmentStructure.keySet());
        }

        // 4. Evaluate each question
        log.info("");
        log.info("========================================");
        log.info("EVALUATING QUESTIONS");
        log.info("========================================");

        List<QuestionResult> questionResults = new ArrayList<>();
        double totalScore = 0.0;
        double maxScore = 0.0;
        double mcqScore = 0.0;
        double mcqMaxScore = 0.0;
        int mcqCorrect = 0;
        int mcqTotal = 0;
        double codingScore = 0.0;
        double codingMaxScore = 0.0;
        int codingPassed = 0;
        int codingTotal = 0;
        int attempted = 0;
        int correct = 0;
        int incorrect = 0;
        int questionNumber = 0;

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> questionsMap =
            (Map<String, List<Map<String, Object>>>) assessmentStructure.get("questionsMap");

        if (questionsMap != null && !questionsMap.isEmpty()) {
            log.info("Processing {} sections from questionsMap", questionsMap.size());
            for (Map.Entry<String, List<Map<String, Object>>> sectionEntry : questionsMap.entrySet()) {
                String sectionId = sectionEntry.getKey();
                List<Map<String, Object>> sectionQuestions = sectionEntry.getValue();

                log.info("");
                log.info("--- Section ID: {} (Questions: {}) ---", sectionId, sectionQuestions != null ? sectionQuestions.size() : 0);

                if (sectionQuestions == null || sectionQuestions.isEmpty()) {
                    log.warn("⚠️  No questions found in section {}", sectionId);
                    continue;
                }

                for (Map<String, Object> question : sectionQuestions) {
                    questionNumber++;
                    String questionId = String.valueOf(question.get("questionId"));
                    String questionType = (String) question.get("type");
                    String questionText = (String) question.get("questionText");
                    Integer points = (Integer) question.get("points");
                    if (points == null) points = 1;

                    maxScore += points;

                    log.info("");
                    log.info("Question #{} (ID: {})", questionNumber, questionId);
                    log.info("  Type: {}", questionType);
                    log.info("  Points: {}", points);
                    if (questionText != null && questionText.length() > 100) {
                        log.info("  Text: {}...", questionText.substring(0, 100));
                    } else {
                        log.info("  Text: {}", questionText);
                    }

                    QuestionResult result = evaluateQuestion(questionId, questionType, answers, question, points, questionNumber);
                    questionResults.add(result);

                    if (result.getUserAnswer() != null && !result.getUserAnswer().isEmpty()) {
                        attempted++;
                    }

                    if (result.getIsCorrect() != null && result.getIsCorrect()) {
                        correct++;
                        totalScore += result.getPointsAwarded();
                    } else if (result.getUserAnswer() != null && !result.getUserAnswer().isEmpty()) {
                        incorrect++;
                    }

                    // Track MCQ vs Coding
                    if ("MCQ".equals(questionType)) {
                        mcqTotal++;
                        mcqMaxScore += points;
                        if (result.getIsCorrect() != null && result.getIsCorrect()) {
                            mcqCorrect++;
                            mcqScore += result.getPointsAwarded();
                        }
                    } else if ("CODING".equals(questionType)) {
                        codingTotal++;
                        codingMaxScore += points;
                        if (result.getIsCorrect() != null && result.getIsCorrect()) {
                            codingPassed++;
                            codingScore += result.getPointsAwarded();
                        }
                    }
                }
            }
        }

        int unanswered = questionResults.size() - attempted;
        double percentageScore = maxScore > 0 ? (totalScore / maxScore) * 100.0 : 0.0;

        // Determine if passed
        Double passingThreshold = request != null && request.getPassingThreshold() != null
            ? request.getPassingThreshold() : 60.0;
        boolean passed = percentageScore >= passingThreshold;

        // Print summary
        log.info("");
        log.info("========================================");
        log.info("EVALUATION SUMMARY");
        log.info("========================================");
        log.info("Total Questions: {}", questionResults.size());
        log.info("Attempted: {}", attempted);
        log.info("Correct: {}", correct);
        log.info("Incorrect: {}", incorrect);
        log.info("Unanswered: {}", unanswered);
        log.info("");
        log.info("MCQ Performance:");
        log.info("  - Total MCQ: {}", mcqTotal);
        log.info("  - Correct: {}", mcqCorrect);
        log.info("  - Score: {}/{} points", mcqScore, mcqMaxScore);
        log.info("");
        log.info("Coding Performance:");
        log.info("  - Total Coding: {}", codingTotal);
        log.info("  - Passed: {}", codingPassed);
        log.info("  - Score: {}/{} points", codingScore, codingMaxScore);
        log.info("");
        log.info("FINAL SCORE: {}/{} points ({}%)", totalScore, maxScore, String.format("%.2f", percentageScore));
        log.info("Status: {} (Threshold: {}%)", passed ? "✓ PASSED" : "✗ FAILED", passingThreshold);
        log.info("========================================");

        // 5. Create evaluation record
        Evaluation evaluation = Evaluation.builder()
                .submissionId(submissionId)
                .totalScore(totalScore)
                .maxScore(maxScore)
                .percentageScore(percentageScore)
                .mcqScore(mcqScore)
                .mcqMaxScore(mcqMaxScore)
                .mcqCorrect(mcqCorrect)
                .mcqTotal(mcqTotal)
                .codingScore(codingScore)
                .codingMaxScore(codingMaxScore)
                .codingPassed(codingPassed)
                .codingTotal(codingTotal)
                .evaluatedAt(Instant.now())
                .questionResults(questionResults)
                .passed(passed)
                .passingThreshold(passingThreshold)
                .totalQuestionsAttempted(attempted)
                .totalQuestionsCorrect(correct)
                .totalQuestionsIncorrect(incorrect)
                .totalQuestionsUnanswered(unanswered)
                .build();

        evaluation = evaluationRepository.save(evaluation);
        log.info("✓ Evaluation saved to MongoDB with ID: {}", evaluation.getId());

        // 6. Update submission with evaluation results
        submission.setTotalScore(totalScore);
        submission.setMaxScore(maxScore);
        submission.setEvaluationId(evaluation.getId());
        submission.setStatus(SubmissionStatus.EVALUATED);
        submissionRepository.save(submission);
        log.info("✓ Submission updated with evaluation results");

        // 7. Sync scores back to AssessmentService
        syncScoresToAssessmentService(metadata, evaluation);

        return mapToResponse(evaluation);
    }

    /**
     * Evaluate a single question
     */
    private QuestionResult evaluateQuestion(String questionId, String questionType,
                                           Map<String, Object> answers,
                                           Map<String, Object> questionData,
                                           Integer points,
                                           int questionNumber) {
        Object userAnswerObj = answers.get(questionId);
        String userAnswer = userAnswerObj != null ? String.valueOf(userAnswerObj) : null;

        QuestionResult.QuestionResultBuilder resultBuilder = QuestionResult.builder()
                .questionId(questionId)
                .questionType(questionType)
                .userAnswer(userAnswer)
                .maxPoints(points.doubleValue())
                .difficulty((String) questionData.get("difficultyLevel"));

        if ("MCQ".equals(questionType)) {
            // Evaluate MCQ question
            return evaluateMCQQuestion(resultBuilder, questionData, userAnswer, points, questionNumber);
        } else if ("CODING".equals(questionType)) {
            // For coding questions, we'll mark as correct if user provided an answer
            boolean hasAnswer = userAnswer != null && !userAnswer.trim().isEmpty();

            log.info("  → User Answer: {}", hasAnswer ? "Code submitted" : "No answer");
            log.info("  → Result: {} ({} points)",
                    hasAnswer ? "✓ SUBMITTED" : "✗ NOT SUBMITTED",
                    hasAnswer ? points : 0);

            resultBuilder
                    .isCorrect(hasAnswer)
                    .pointsAwarded(hasAnswer ? points.doubleValue() : 0.0)
                    .feedback(hasAnswer ? "Code submitted" : "No code submitted");
        } else {
            log.info("  → Unknown question type");
            resultBuilder
                    .isCorrect(false)
                    .pointsAwarded(0.0)
                    .feedback("Unknown question type");
        }

        return resultBuilder.build();
    }

    /**
     * Evaluate MCQ question by checking correct answer
     */
    private QuestionResult evaluateMCQQuestion(QuestionResult.QuestionResultBuilder resultBuilder,
                                              Map<String, Object> questionData,
                                              String userAnswer,
                                              Integer points,
                                              int questionNumber) {
        // Get MCQ options from question data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mcqOptions = (List<Map<String, Object>>) questionData.get("mcqOptions");

        if (mcqOptions == null || mcqOptions.isEmpty()) {
            log.info("  → ⚠️  No MCQ options available");
            return resultBuilder
                    .isCorrect(false)
                    .pointsAwarded(0.0)
                    .correctAnswer("N/A")
                    .feedback("No options available")
                    .build();
        }

        // Find correct answer and match user's answer
        String correctAnswerLabel = null;
        String correctAnswerText = null;
        String userAnswerLabel = null;

        // First, find the correct answer
        for (Map<String, Object> option : mcqOptions) {
            Boolean isCorrect = (Boolean) option.get("isCorrect");
            if (isCorrect != null && isCorrect) {
                correctAnswerLabel = (String) option.get("optionLabel");
                correctAnswerText = (String) option.get("optionText");
                break;
            }
        }

        if (correctAnswerLabel == null) {
            log.info("  → ⚠️  No correct answer marked in database");
            return resultBuilder
                    .isCorrect(false)
                    .pointsAwarded(0.0)
                    .correctAnswer("N/A")
                    .feedback("No correct answer marked")
                    .build();
        }

        // Now determine what the user selected
        // User's answer could be:
        // 1. Option label (A, B, C, D)
        // 2. Option text (full text of the option)
        // 3. Option ID (numeric ID)

        if (userAnswer != null && !userAnswer.isEmpty()) {
            // Check if user answer matches any option label (A, B, C, D)
            if (userAnswer.length() <= 2 && userAnswer.matches("[A-Da-d]")) {
                // User selected by label (A, B, C, D)
                userAnswerLabel = userAnswer.toUpperCase();
            } else {
                // User might have selected by option text or ID
                // Find which option matches the user's answer
                for (Map<String, Object> option : mcqOptions) {
                    String optionLabel = (String) option.get("optionLabel");
                    String optionText = (String) option.get("optionText");
                    Object optionIdObj = option.get("optionId");
                    String optionId = optionIdObj != null ? String.valueOf(optionIdObj) : null;

                    // Check if user answer matches option text
                    if (optionText != null && optionText.equals(userAnswer)) {
                        userAnswerLabel = optionLabel;
                        break;
                    }
                    // Check if user answer matches option ID
                    if (optionId != null && optionId.equals(userAnswer)) {
                        userAnswerLabel = optionLabel;
                        break;
                    }
                }

                // If we still couldn't find a match, use the original answer
                if (userAnswerLabel == null) {
                    userAnswerLabel = userAnswer;
                }
            }
        }

        // Compare user answer with correct answer
        boolean isAnswerCorrect = correctAnswerLabel.equalsIgnoreCase(userAnswerLabel);

        // Detailed logging
        log.info("  → User Selected: {} (mapped to: {})",
                userAnswer != null ? userAnswer : "NOT ANSWERED",
                userAnswerLabel != null ? userAnswerLabel : "N/A");
        log.info("  → Correct Answer: {} ({})", correctAnswerLabel,
                correctAnswerText != null ? correctAnswerText : "");

        if (userAnswer == null || userAnswer.isEmpty()) {
            log.info("  → Result: ⊘ NOT ATTEMPTED (0/{} points)", points);
        } else if (isAnswerCorrect) {
            log.info("  → Result: ✓ CORRECT ({}/{} points)", points, points);
        } else {
            log.info("  → Result: ✗ WRONG (0/{} points)", points);
        }

        return resultBuilder
                .correctAnswer(correctAnswerLabel)
                .isCorrect(isAnswerCorrect)
                .pointsAwarded(isAnswerCorrect ? points.doubleValue() : 0.0)
                .feedback(isAnswerCorrect ? "Correct!" :
                         (userAnswer == null ? "Not attempted" : "Incorrect. Correct answer: " + correctAnswerLabel))
                .build();
    }

    /**
     * Fetch assessment structure from AssessmentService
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchAssessmentStructure(String testId) {
        try {
            String url = ASSESSMENT_SERVICE_URL + "/assessments/" + testId + "/structure";
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Error fetching assessment structure for testId {}: {}", testId, e.getMessage());
            throw new RuntimeException("Failed to fetch assessment structure: " + e.getMessage());
        }
    }

    /**
     * Get evaluation by ID
     */
    public EvaluationResponse getEvaluation(String evaluationId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found: " + evaluationId));
        return mapToResponse(evaluation);
    }

    /**
     * Get evaluation by submission ID
     */
    public EvaluationResponse getEvaluationBySubmissionId(String submissionId) {
        Evaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found for submission: " + submissionId));
        return mapToResponse(evaluation);
    }

    /**
     * Map Evaluation entity to response DTO
     */
    private EvaluationResponse mapToResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .submissionId(evaluation.getSubmissionId())
                .totalScore(evaluation.getTotalScore())
                .maxScore(evaluation.getMaxScore())
                .percentageScore(evaluation.getPercentageScore())
                .mcqScore(evaluation.getMcqScore())
                .mcqMaxScore(evaluation.getMcqMaxScore())
                .mcqCorrect(evaluation.getMcqCorrect())
                .mcqTotal(evaluation.getMcqTotal())
                .codingScore(evaluation.getCodingScore())
                .codingMaxScore(evaluation.getCodingMaxScore())
                .codingPassed(evaluation.getCodingPassed())
                .codingTotal(evaluation.getCodingTotal())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .evaluatorId(evaluation.getEvaluatorId())
                .remarks(evaluation.getRemarks())
                .breakdown(evaluation.getBreakdown())
                .detailedResults(evaluation.getDetailedResults())
                .questionResults(evaluation.getQuestionResults())
                .passed(evaluation.getPassed())
                .passingThreshold(evaluation.getPassingThreshold())
                .totalQuestionsAttempted(evaluation.getTotalQuestionsAttempted())
                .totalQuestionsCorrect(evaluation.getTotalQuestionsCorrect())
                .totalQuestionsIncorrect(evaluation.getTotalQuestionsIncorrect())
                .totalQuestionsUnanswered(evaluation.getTotalQuestionsUnanswered())
                .build();
    }

    /**
     * Sync evaluation scores back to AssessmentService
     */
    private void syncScoresToAssessmentService(Map<String, Object> metadata, Evaluation evaluation) {
        try {
            // Get the assessment candidate ID from metadata
            Long assessmentCandidateId = null;
            if (metadata.containsKey("assessmentCandidateId")) {
                Object candidateIdObj = metadata.get("assessmentCandidateId");
                if (candidateIdObj instanceof Number) {
                    assessmentCandidateId = ((Number) candidateIdObj).longValue();
                } else if (candidateIdObj instanceof String) {
                    assessmentCandidateId = Long.parseLong((String) candidateIdObj);
                }
            }

            if (assessmentCandidateId == null) {
                log.warn("⚠️  No assessmentCandidateId found in metadata, skipping score sync to AssessmentService");
                return;
            }

            // Prepare score update request
            Map<String, Object> scoreRequest = new HashMap<>();
            scoreRequest.put("assessmentCandidateId", assessmentCandidateId);
            scoreRequest.put("totalScore", evaluation.getTotalScore());
            scoreRequest.put("maxScore", evaluation.getMaxScore());
            scoreRequest.put("percentageScore", evaluation.getPercentageScore());
            scoreRequest.put("isPassed", evaluation.getPassed());
            scoreRequest.put("totalQuestions", evaluation.getQuestionResults().size());
            scoreRequest.put("attemptedQuestions", evaluation.getTotalQuestionsAttempted());
            scoreRequest.put("correctAnswers", evaluation.getTotalQuestionsCorrect());
            scoreRequest.put("incorrectAnswers", evaluation.getTotalQuestionsIncorrect());
            scoreRequest.put("unansweredQuestions", evaluation.getTotalQuestionsUnanswered());
            scoreRequest.put("mcqCorrect", evaluation.getMcqCorrect());
            scoreRequest.put("mcqTotal", evaluation.getMcqTotal());
            scoreRequest.put("codingPassed", evaluation.getCodingPassed());
            scoreRequest.put("codingTotal", evaluation.getCodingTotal());

            String updateScoreUrl = ASSESSMENT_SERVICE_URL + "/assessments/candidates/" + assessmentCandidateId + "/score";
            restTemplate.put(updateScoreUrl, scoreRequest);

            log.info("✓ Synced evaluation scores to AssessmentService for candidate ID: {}", assessmentCandidateId);
        } catch (Exception e) {
            log.error("❌ Failed to sync scores to AssessmentService: {}", e.getMessage());
            // Don't fail the evaluation if sync fails
        }
    }
}
