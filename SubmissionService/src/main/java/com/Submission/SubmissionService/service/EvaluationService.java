package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.domain.*;
import com.Submission.SubmissionService.dto.EvaluateSubmissionRequest;
import com.Submission.SubmissionService.dto.EvaluationResponse;
import com.Submission.SubmissionService.dto.RunCodeRequest;
import com.Submission.SubmissionService.dto.RunCodeResponse;
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
    private final PistonApiService pistonApiService;
    private final NotificationPublisher notificationPublisher;

    private static final String ASSESSMENT_SERVICE_URL = "http://assessment-service:8081";
    private static final String QUESTION_SERVICE_URL = "http://question-service:8082";
    private static final String AUTH_SERVICE_URL = "http://auth-service:8080";

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

                    // Always add points awarded to total score (supports partial credit)
                    totalScore += result.getPointsAwarded();

                    // Track correct/incorrect based on isCorrect flag
                    if (result.getIsCorrect() != null && result.getIsCorrect()) {
                        correct++;
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
                        // Always add points awarded for coding questions (supports partial credit)
                        codingScore += result.getPointsAwarded();
                        // Only increment "passed" counter if ALL test cases passed
                        if (result.getIsCorrect() != null && result.getIsCorrect()) {
                            codingPassed++;
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

        // 8. Send notification about evaluation completion
        sendEvaluationNotification(submission, evaluation);

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
            // Evaluate coding question with test cases
            return evaluateCodingQuestion(resultBuilder, questionData, userAnswer, points, questionNumber);
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
        if (userAnswer != null && !userAnswer.isEmpty()) {
            // Check if user answer matches any option label (A, B, C, D)
            if (userAnswer.length() <= 2 && userAnswer.matches("[A-Da-d]")) {
                userAnswerLabel = userAnswer.toUpperCase();
            } else {
                // User might have selected by option text or ID
                for (Map<String, Object> option : mcqOptions) {
                    String optionLabel = (String) option.get("optionLabel");
                    String optionText = (String) option.get("optionText");
                    Object optionIdObj = option.get("optionId");
                    String optionId = optionIdObj != null ? String.valueOf(optionIdObj) : null;

                    if (optionText != null && optionText.equals(userAnswer)) {
                        userAnswerLabel = optionLabel;
                        break;
                    }
                    if (optionId != null && optionId.equals(userAnswer)) {
                        userAnswerLabel = optionLabel;
                        break;
                    }
                }

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
     * Evaluate coding question by checking stored test case results (no re-execution)
     */
    private QuestionResult evaluateCodingQuestion(QuestionResult.QuestionResultBuilder resultBuilder,
                                                  Map<String, Object> questionData,
                                                  String userCode,
                                                  Integer points,
                                                  int questionNumber) {
        // Check if user submitted code
        if (userCode == null || userCode.trim().isEmpty()) {
            log.info("  → User Answer: No code submitted");
            log.info("  → Result: ✗ NOT SUBMITTED (0/{} points)", points);

            return resultBuilder
                    .isCorrect(false)
                    .pointsAwarded(0.0)
                    .totalTestCases(0)
                    .passedTestCases(0)
                    .feedback("No code submitted")
                    .build();
        }

        // Get test cases from question data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> testCases = (List<Map<String, Object>>) questionData.get("testCases");

        if (testCases == null || testCases.isEmpty()) {
            log.info("  → ⚠️  No test cases available for this coding question");
            log.info("  → Result: ✓ CODE SUBMITTED (Full points awarded)");

            // If no test cases, award full points for submitting code
            return resultBuilder
                    .isCorrect(true)
                    .pointsAwarded(points.doubleValue())
                    .totalTestCases(0)
                    .passedTestCases(0)
                    .feedback("Code submitted (no test cases to validate)")
                    .build();
        }

        // Check if test case results are already stored in questionData (from submission)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> storedResults = (List<Map<String, Object>>) questionData.get("testCaseResults");

        List<Map<String, Object>> testCaseResults;
        if (storedResults != null && !storedResults.isEmpty()) {
            log.info("  → Using pre-computed test case results from submission");
            testCaseResults = storedResults;
        } else {
            log.info("  → No stored results found, computing test case results");
            testCaseResults = fetchAndRunTestCases(questionData, userCode, testCases);
        }

        // Check if there was an execution error
        boolean hasExecutionError = testCaseResults.stream()
                .anyMatch(result -> Boolean.TRUE.equals(result.get("executionError")));

        if (hasExecutionError) {
            // Get the error message from the first test case with error
            Map<String, Object> errorResult = testCaseResults.stream()
                    .filter(result -> Boolean.TRUE.equals(result.get("executionError")))
                    .findFirst()
                    .orElse(null);

            String errorMessage = errorResult != null ?
                (String) errorResult.get("actualOutput") : "Code execution failed";

            // Extract readable error message
            String cleanError = errorMessage;
            if (errorMessage.startsWith("EXECUTION_ERROR:")) {
                cleanError = errorMessage.substring("EXECUTION_ERROR:".length()).trim();
                // Limit error message length
                if (cleanError.length() > 200) {
                    cleanError = cleanError.substring(0, 200) + "...";
                }
            }

            log.info("  → User Answer: Code submitted");
            log.info("  → Result: ⚠️ EXECUTION ERROR");
            log.warn("  → Error: {}", cleanError.substring(0, Math.min(100, cleanError.length())));

            return resultBuilder
                    .isCorrect(null) // null means execution error, not wrong answer
                    .pointsAwarded(0.0)
                    .totalTestCases(testCases.size())
                    .passedTestCases(0)
                    .testCaseResults(testCaseResults)
                    .feedback("Code execution failed: " + cleanError)
                    .build();
        }

        // Calculate score based on passed test cases
        int totalTestCases = testCases.size();
        int passedTestCases = (int) testCaseResults.stream()
                .filter(result -> Boolean.TRUE.equals(result.get("passed")))
                .count();

        // Calculate points: (passed / total) * maxPoints
        double awardedPoints = totalTestCases > 0
                ? ((double) passedTestCases / totalTestCases) * points
                : 0.0;

        boolean allPassed = passedTestCases == totalTestCases;

        log.info("  → User Answer: Code submitted");
        log.info("  → Test Cases: {}/{} passed", passedTestCases, totalTestCases);
        log.info("  → Result: {} ({}/{} points)",
                allPassed ? "✓ ALL TESTS PASSED" : "⚠ PARTIAL SUCCESS",
                String.format("%.2f", awardedPoints), points);

        return resultBuilder
                .isCorrect(allPassed)
                .pointsAwarded(awardedPoints)
                .totalTestCases(totalTestCases)
                .passedTestCases(passedTestCases)
                .testCaseResults(testCaseResults)
                .feedback(String.format("Passed %d out of %d test cases", passedTestCases, totalTestCases))
                .build();
    }

    /**
     * Fetch test cases from QuestionService and run them against user code
     */
    private List<Map<String, Object>> fetchAndRunTestCases(Map<String, Object> questionData,
                                                            String userCode,
                                                            List<Map<String, Object>> testCases) {
        List<Map<String, Object>> results = new ArrayList<>();

        String programmingLanguage = (String) questionData.get("programmingLanguage");

        // If no language specified or seems wrong, try to detect from code
        String detectedLanguage = detectLanguageFromCode(userCode);
        if (programmingLanguage == null || programmingLanguage.isEmpty()) {
            log.warn("    No programmingLanguage specified in question data, using detected language: {}", detectedLanguage);
            programmingLanguage = detectedLanguage;
        } else if (!isCodeMatchingLanguage(userCode, programmingLanguage)) {
            log.warn("    Code doesn't match specified language '{}', detected language: {}", programmingLanguage, detectedLanguage);
            log.warn("    Using detected language instead");
            programmingLanguage = detectedLanguage;
        }

        log.info("    Executing code as: {}", programmingLanguage);

        for (int i = 0; i < testCases.size(); i++) {
            Map<String, Object> testCase = testCases.get(i);
            Map<String, Object> result = new HashMap<>();

            String input = (String) testCase.get("inputData");
            String expectedOutput = (String) testCase.get("expectedOutput");
            Boolean isSample = (Boolean) testCase.get("isSample");

            // Run the test case
            String actualOutput = executeCodeWithInput(userCode, input, programmingLanguage);

            // Check if it's an execution error
            boolean isExecutionError = actualOutput != null && actualOutput.startsWith("EXECUTION_ERROR:");
            boolean passed = !isExecutionError && compareOutputs(expectedOutput, actualOutput);

            result.put("testCaseNumber", i + 1);
            result.put("input", isSample != null && isSample ? input : "Hidden");
            result.put("expectedOutput", isSample != null && isSample ? expectedOutput : "Hidden");
            result.put("actualOutput", isExecutionError ? "Execution Error" : actualOutput);
            result.put("passed", passed);
            result.put("isSample", isSample);
            result.put("executionError", isExecutionError);

            // Store the full error for the first test case (for feedback)
            if (isExecutionError && i == 0) {
                result.put("actualOutput", actualOutput); // Keep full error for feedback
            }

            results.add(result);

            if (isExecutionError) {
                log.debug("    Test Case #{}: EXECUTION ERROR", i + 1);
            } else {
                log.debug("    Test Case #{}: {}", i + 1, passed ? "PASSED" : "FAILED");
            }
        }

        return results;
    }

    /**
     * Detect programming language from code content
     */
    private String detectLanguageFromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "python"; // default
        }

        String trimmedCode = code.trim();

        // C++ detection
        if (trimmedCode.contains("#include") &&
            (trimmedCode.contains("iostream") || trimmedCode.contains("bits/stdc++.h") ||
             trimmedCode.contains("using namespace std") || trimmedCode.contains("std::"))) {
            return "c++";
        }

        // C detection (has includes but no C++ features)
        if (trimmedCode.contains("#include") && !trimmedCode.contains("namespace") &&
            !trimmedCode.contains("std::") && !trimmedCode.contains("cout") && !trimmedCode.contains("cin")) {
            return "c";
        }

        // Java detection
        if (trimmedCode.contains("public class") || trimmedCode.contains("public static void main") ||
            trimmedCode.contains("System.out.println") || trimmedCode.contains("import java.")) {
            return "java";
        }

        // JavaScript detection
        if (trimmedCode.contains("console.log") || trimmedCode.contains("const ") ||
            trimmedCode.contains("let ") || trimmedCode.contains("=>") ||
            trimmedCode.contains("function ") || trimmedCode.contains("var ")) {
            return "javascript";
        }

        // Python detection (default if nothing else matches)
        if (trimmedCode.contains("def ") || trimmedCode.contains("import ") ||
            trimmedCode.contains("print(") || trimmedCode.contains("if __name__")) {
            return "python";
        }

        // Default to python if can't detect
        return "python";
    }

    /**
     * Check if code matches the specified language
     */
    private boolean isCodeMatchingLanguage(String code, String language) {
        if (code == null || language == null) {
            return true; // can't verify, assume it's okay
        }

        String detectedLanguage = detectLanguageFromCode(code);
        String normalizedSpecified = normalizeLanguage(language);
        String normalizedDetected = normalizeLanguage(detectedLanguage);

        return normalizedSpecified.equals(normalizedDetected);
    }

    /**
     * Execute user code with given input using Piston API
     */
    private String executeCodeWithInput(String code, String input, String language) {
        try {
            log.debug("    Executing {} code with Piston API", language);

            // Normalize language name
            String normalizedLanguage = normalizeLanguage(language);

            // Execute code using Piston API
            RunCodeRequest runRequest = new RunCodeRequest();
            runRequest.setLanguage(normalizedLanguage);
            runRequest.setCode(code);
            runRequest.setStdin(input != null ? input : "");
            runRequest.setVersion("*"); // Latest version
            runRequest.setRunTimeout(5000);
            runRequest.setCompileTimeout(10000);
            runRequest.setCompileMemoryLimit(-1L);
            runRequest.setRunMemoryLimit(-1L);

            // Call Piston API
            RunCodeResponse response = pistonApiService.executeCode(runRequest);

            if (response == null || response.getRun() == null) {
                log.error("    No response from Piston API");
                return "EXECUTION_ERROR:No response from execution service";
            }

            RunCodeResponse.RunResult runResult = response.getRun();

            // Check for execution errors
            if (runResult.getCode() != null && runResult.getCode() != 0) {
                String stderr = runResult.getStderr();
                if (stderr != null && !stderr.isEmpty()) {
                    log.warn("    Code execution failed: {}", stderr);
                    return "EXECUTION_ERROR:" + stderr;
                }
                return "EXECUTION_ERROR:Execution failed with exit code " + runResult.getCode();
            }

            // Successful execution - return stdout
            String stdout = runResult.getStdout();
            if (stdout != null && !stdout.isEmpty()) {
                return stdout;
            }

            // Check for output field
            String output = runResult.getOutput();
            if (output != null && !output.isEmpty()) {
                return output;
            }

            // No output generated
            String stderr = runResult.getStderr();
            if (stderr != null && !stderr.isEmpty()) {
                log.warn("    No stdout but has stderr");
                return "EXECUTION_ERROR:" + stderr;
            }

            log.warn("    Code execution returned no output");
            return "EXECUTION_ERROR:No output generated";

        } catch (Exception e) {
            log.error("    Error executing code: {}", e.getMessage(), e);
            return "EXECUTION_ERROR:" + e.getMessage();
        }
    }

    /**
     * Normalize language name for Piston API
     */
    private String normalizeLanguage(String language) {
        if (language == null) {
            return "python";
        }

        switch (language.toLowerCase().trim()) {
            case "python":
            case "python3":
            case "py":
                return "python";
            case "javascript":
            case "js":
            case "node":
            case "nodejs":
                return "javascript";
            case "java":
                return "java";
            case "c++":
            case "cpp":
            case "cplusplus":
                return "c++";
            case "c":
                return "c";
            default:
                log.warn("Unknown language '{}', defaulting to python", language);
                return "python";
        }
    }

    /**
     * Compare expected and actual outputs
     */
    private boolean compareOutputs(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        // Normalize outputs (trim whitespace, normalize line endings)
        String normalizedExpected = expected.trim().replaceAll("\\r\\n", "\n");
        String normalizedActual = actual.trim().replaceAll("\\r\\n", "\n");

        return normalizedExpected.equals(normalizedActual);
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

    /**
     * Send notification about evaluation completion
     */
    private void sendEvaluationNotification(Submission submission, Evaluation evaluation) {
        try {
            String userId = submission.getUserId();
            Map<String, Object> metadata = submission.getMetadata();

            // Get assessment name and company name from metadata
            String assessmentName = (String) metadata.get("assessmentName");
            if (assessmentName == null) {
                assessmentName = "Assessment";
            }

            String companyName = (String) metadata.get("companyName");
            if (companyName == null) {
                companyName = "Company";
            }

            // Fetch user details from AuthService
            Map<String, Object> userDetails = fetchUserDetails(userId);
            String userEmail = (String) userDetails.get("email");
            String userName = (String) userDetails.get("username");

            // Calculate percentage and pass status
            double percentage = evaluation.getPercentageScore();
            boolean isPassed = evaluation.getPassed() != null ? evaluation.getPassed() : false;

            // Convert userId String to Integer
            Integer userIdInt;
            try {
                userIdInt = Integer.parseInt(userId);
            } catch (NumberFormatException e) {
                log.error("Failed to parse userId to Integer: {}", userId);
                userIdInt = 0;
            }

            // Send evaluation completed notification
            notificationPublisher.sendEvaluationCompletedNotification(
                userIdInt,
                userEmail,
                userName,
                assessmentName,
                companyName,
                evaluation.getTotalScore(),
                evaluation.getMaxScore()
            );

            log.info("✓ Evaluation completion notification sent for user: {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to send evaluation notification: {}", e.getMessage());
            // Don't fail the evaluation if notification fails
        }
    }

    /**
     * Fetch user details from AuthService
     */
    private Map<String, Object> fetchUserDetails(String userId) {
        // First try to get user details from AuthService
        try {
            String url = AUTH_SERVICE_URL + "/auth/users/" + userId;
            log.info("Fetching user details from: {}", url);
            Map<String, Object> userDetails = restTemplate.getForObject(url, Map.class);
            if (userDetails != null && userDetails.get("email") != null) {
                return userDetails;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user details from AuthService: {}", e.getMessage());
        }
        
        // If AuthService fails, try to get email from submission metadata
        try {
            // Get submission for this user
            List<Submission> userSubmissions = submissionRepository.findByUserId(userId);
            if (!userSubmissions.isEmpty()) {
                // Get the most recent submission
                Submission latestSubmission = userSubmissions.get(0);
                Map<String, Object> metadata = latestSubmission.getMetadata();
                if (metadata != null) {
                    String userEmail = (String) metadata.get("userEmail");
                    String userName = (String) metadata.get("userName");
                    
                    if (userEmail != null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("email", userEmail);
                        result.put("username", userName != null ? userName : "User" + userId);
                        log.info("Using email from submission metadata: {}", userEmail);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get user details from submission metadata: {}", e.getMessage());
        }
        
        // If all else fails, log an error and use the example email as last resort
        log.error("Could not determine user email for userId: {}. Using fallback email.", userId);
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("email", "user" + userId + "@example.com");
        fallback.put("username", "User" + userId);
        return fallback;
    }
}
