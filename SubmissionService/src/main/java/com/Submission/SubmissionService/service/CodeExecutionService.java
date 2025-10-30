package com.Submission.SubmissionService.service;

import com.Submission.SubmissionService.domain.CodeExecution;
import com.Submission.SubmissionService.domain.ExecutionStatus;
import com.Submission.SubmissionService.domain.SubmissionFile;
import com.Submission.SubmissionService.dto.CodeExecutionResponse;
import com.Submission.SubmissionService.dto.ExecuteCodeRequest;
import com.Submission.SubmissionService.repository.CodeExecutionRepository;
import com.Submission.SubmissionService.repository.SubmissionFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionService {

    private final CodeExecutionRepository executionRepository;
    private final SubmissionFileRepository fileRepository;

    public CodeExecutionResponse executeCode(ExecuteCodeRequest request) {
        log.info("Executing code for submission {} and file {}", 
                request.getSubmissionId(), request.getFileId());

        SubmissionFile file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new RuntimeException("File not found: " + request.getFileId()));

        CodeExecution execution = CodeExecution.builder()
                .submissionId(request.getSubmissionId())
                .fileId(request.getFileId())
                .questionId(request.getQuestionId())
                .language(file.getLanguage())
                .status(ExecutionStatus.RUNNING)
                .executedAt(Instant.now())
                .build();

        execution = executionRepository.save(execution);

        try {
            Map<String, Object> executionResult = simulateExecution(
                    file.getContent(), 
                    file.getLanguage(), 
                    request.getTestCases()
            );

            execution.setStatus((ExecutionStatus) executionResult.get("status"));
            execution.setRunTimeMs((Long) executionResult.get("runTimeMs"));
            execution.setMemoryUsedMb((Long) executionResult.get("memoryUsedMb"));
            execution.setExitCode((Integer) executionResult.get("exitCode"));
            execution.setStdout((String) executionResult.get("stdout"));
            execution.setStderr((String) executionResult.get("stderr"));
            execution.setTestCaseResults((List<Map<String, Object>>) executionResult.get("testCaseResults"));
            execution.setPassedCount((Integer) executionResult.get("passedCount"));
            execution.setTotalCount((Integer) executionResult.get("totalCount"));
            execution.setScore((Double) executionResult.get("score"));

        } catch (Exception e) {
            log.error("Execution failed: ", e);
            execution.setStatus(ExecutionStatus.ERROR);
            execution.setStderr(e.getMessage());
            execution.setPassedCount(0);
            execution.setTotalCount(request.getTestCases() != null ? request.getTestCases().size() : 0);
            execution.setScore(0.0);
        }

        execution = executionRepository.save(execution);
        log.info("Code execution completed with status: {}", execution.getStatus());

        return mapToResponse(execution);
    }

    public CodeExecutionResponse getExecution(String id) {
        log.info("Fetching execution with id: {}", id);
        CodeExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + id));
        return mapToResponse(execution);
    }

    public List<CodeExecutionResponse> getExecutionsBySubmission(String submissionId) {
        log.info("Fetching executions for submission: {}", submissionId);
        return executionRepository.findBySubmissionIdOrderByExecutedAtDesc(submissionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CodeExecutionResponse> getExecutionsByQuestion(String questionId) {
        log.info("Fetching executions for question: {}", questionId);
        return executionRepository.findByQuestionId(questionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> simulateExecution(String code, String language, List<Map<String, Object>> testCases) {
        Map<String, Object> result = new HashMap<>();
        
        if (code == null || code.trim().isEmpty()) {
            result.put("status", ExecutionStatus.COMPILATION_ERROR);
            result.put("stderr", "Empty code submission");
            result.put("exitCode", 1);
            result.put("runTimeMs", 0L);
            result.put("memoryUsedMb", 0L);
            result.put("passedCount", 0);
            result.put("totalCount", testCases != null ? testCases.size() : 0);
            result.put("score", 0.0);
            result.put("testCaseResults", new ArrayList<>());
            return result;
        }

        List<Map<String, Object>> testCaseResults = new ArrayList<>();
        int passedCount = 0;
        int totalCount = testCases != null ? testCases.size() : 0;

        if (testCases != null) {
            for (int i = 0; i < testCases.size(); i++) {
                Map<String, Object> testCase = testCases.get(i);
                Map<String, Object> testResult = new HashMap<>();
                
                testResult.put("testCaseId", testCase.getOrDefault("id", "test_" + i));
                testResult.put("passed", Math.random() > 0.3);
                testResult.put("expected", testCase.get("expectedOutput"));
                testResult.put("actual", "Simulated output");
                testResult.put("timeMs", (long) (Math.random() * 100));
                
                if ((Boolean) testResult.get("passed")) {
                    passedCount++;
                }
                
                testCaseResults.add(testResult);
            }
        }

        double score = totalCount > 0 ? (double) passedCount / totalCount * 100 : 0.0;

        result.put("status", passedCount == totalCount ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
        result.put("runTimeMs", (long) (Math.random() * 500));
        result.put("memoryUsedMb", (long) (Math.random() * 100));
        result.put("exitCode", 0);
        result.put("stdout", "Execution completed");
        result.put("stderr", "");
        result.put("testCaseResults", testCaseResults);
        result.put("passedCount", passedCount);
        result.put("totalCount", totalCount);
        result.put("score", score);

        return result;
    }

    private CodeExecutionResponse mapToResponse(CodeExecution execution) {
        return CodeExecutionResponse.builder()
                .id(execution.getId())
                .submissionId(execution.getSubmissionId())
                .fileId(execution.getFileId())
                .questionId(execution.getQuestionId())
                .language(execution.getLanguage())
                .executedAt(execution.getExecutedAt())
                .runTimeMs(execution.getRunTimeMs())
                .memoryUsedMb(execution.getMemoryUsedMb())
                .exitCode(execution.getExitCode())
                .status(execution.getStatus())
                .stdout(execution.getStdout())
                .stderr(execution.getStderr())
                .compileOutput(execution.getCompileOutput())
                .testCaseResults(execution.getTestCaseResults())
                .passedCount(execution.getPassedCount())
                .totalCount(execution.getTotalCount())
                .score(execution.getScore())
                .artifactPath(execution.getArtifactPath())
                .metadata(execution.getMetadata())
                .build();
    }
}

