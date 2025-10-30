package com.Submission.SubmissionService.controller;

import com.Submission.SubmissionService.domain.SubmissionStatus;
import com.Submission.SubmissionService.dto.CreateSubmissionRequest;
import com.Submission.SubmissionService.dto.SubmissionResponse;
import com.Submission.SubmissionService.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(@RequestBody CreateSubmissionRequest request) {
        log.info("Received submission creation request for user: {} and test: {}", request.getUserId(), request.getTestId());
        try {
            SubmissionResponse response = submissionService.createSubmission(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating submission", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable String id) {
        log.info("Fetching submission: {}", id);
        SubmissionResponse response = submissionService.getSubmission(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SubmissionResponse>> getAllSubmissions() {
        log.info("Fetching all submissions");
        List<SubmissionResponse> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByUser(@PathVariable String userId) {
        log.info("Fetching submissions for user: {}", userId);
        List<SubmissionResponse> submissions = submissionService.getSubmissionsByUserId(userId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByTest(@PathVariable String testId) {
        log.info("Fetching submissions for test: {}", testId);
        List<SubmissionResponse> submissions = submissionService.getSubmissionsByTestId(testId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/user/{userId}/test/{testId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByUserAndTest(
            @PathVariable String userId,
            @PathVariable String testId) {
        log.info("Fetching submissions for user: {} and test: {}", userId, testId);
        List<SubmissionResponse> submissions = submissionService.getSubmissionsByUserAndTest(userId, testId);
        return ResponseEntity.ok(submissions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SubmissionResponse> updateSubmissionStatus(
            @PathVariable String id,
            @RequestParam SubmissionStatus status) {
        log.info("Updating submission {} status to {}", id, status);
        SubmissionResponse response = submissionService.updateSubmissionStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/score")
    public ResponseEntity<SubmissionResponse> updateSubmissionScore(
            @PathVariable String id,
            @RequestParam Double totalScore,
            @RequestParam Double maxScore) {
        log.info("Updating submission {} score to {}/{}", id, totalScore, maxScore);
        SubmissionResponse response = submissionService.updateSubmissionScore(id, totalScore, maxScore);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable String id) {
        log.info("Deleting submission: {}", id);
        submissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}

