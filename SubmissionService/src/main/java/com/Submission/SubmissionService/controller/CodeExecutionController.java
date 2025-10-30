package com.Submission.SubmissionService.controller;

import com.Submission.SubmissionService.dto.CodeExecutionResponse;
import com.Submission.SubmissionService.dto.ExecuteCodeRequest;
import com.Submission.SubmissionService.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CodeExecutionController {

    private final CodeExecutionService executionService;

    @PostMapping
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @Valid @RequestBody ExecuteCodeRequest request) {
        log.info("POST /api/executions - Executing code");
        CodeExecutionResponse response = executionService.executeCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeExecutionResponse> getExecution(@PathVariable String id) {
        log.info("GET /api/executions/{} - Fetching execution", id);
        CodeExecutionResponse response = executionService.getExecution(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<CodeExecutionResponse>> getExecutionsBySubmission(
            @PathVariable String submissionId) {
        log.info("GET /api/executions/submission/{} - Fetching executions by submission", submissionId);
        List<CodeExecutionResponse> responses = executionService.getExecutionsBySubmission(submissionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<CodeExecutionResponse>> getExecutionsByQuestion(
            @PathVariable String questionId) {
        log.info("GET /api/executions/question/{} - Fetching executions by question", questionId);
        List<CodeExecutionResponse> responses = executionService.getExecutionsByQuestion(questionId);
        return ResponseEntity.ok(responses);
    }
}

