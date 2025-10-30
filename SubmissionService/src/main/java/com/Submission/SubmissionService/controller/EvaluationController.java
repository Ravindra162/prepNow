package com.Submission.SubmissionService.controller;

import com.Submission.SubmissionService.dto.EvaluateSubmissionRequest;
import com.Submission.SubmissionService.dto.EvaluationResponse;
import com.Submission.SubmissionService.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Evaluate a submission - Main API endpoint
     * POST /api/evaluations/submission/{submissionId}
     */
    @PostMapping("/submission/{submissionId}")
    public ResponseEntity<EvaluationResponse> evaluateSubmission(
            @PathVariable String submissionId,
            @RequestBody(required = false) EvaluateSubmissionRequest request) {
        log.info("POST /api/evaluations/submission/{} - Starting evaluation", submissionId);

        try {
            EvaluationResponse response = evaluationService.evaluateSubmission(submissionId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error evaluating submission {}: {}", submissionId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get evaluation by evaluation ID
     * GET /api/evaluations/{evaluationId}
     */
    @GetMapping("/{evaluationId}")
    public ResponseEntity<EvaluationResponse> getEvaluation(@PathVariable String evaluationId) {
        log.info("GET /api/evaluations/{} - Fetching evaluation", evaluationId);
        EvaluationResponse response = evaluationService.getEvaluation(evaluationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get evaluation by submission ID
     * GET /api/evaluations/submission/{submissionId}
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<EvaluationResponse> getEvaluationBySubmission(
            @PathVariable String submissionId) {
        log.info("GET /api/evaluations/submission/{} - Fetching evaluation by submission", submissionId);
        EvaluationResponse response = evaluationService.getEvaluationBySubmissionId(submissionId);
        return ResponseEntity.ok(response);
    }
}
