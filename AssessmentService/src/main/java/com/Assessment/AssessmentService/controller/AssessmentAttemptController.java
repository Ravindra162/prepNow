package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.service.AssessmentAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
public class AssessmentAttemptController {
    
    private final AssessmentAttemptService assessmentAttemptService;
    
    /**
     * Start an assessment attempt for a candidate
     */
    @PostMapping("/{assessmentId}/attempt")
    public ResponseEntity<Map<String, Object>> startAssessmentAttempt(
            @PathVariable Long assessmentId,
            @RequestParam Integer userRef) {
        try {
            Map<String, Object> attemptData = assessmentAttemptService.startAssessmentAttempt(assessmentId, userRef);
            return new ResponseEntity<>(attemptData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get assessment attempt data including sections and questions
     */
    @GetMapping("/{assessmentId}/attempt")
    public ResponseEntity<Map<String, Object>> getAssessmentAttemptData(
            @PathVariable Long assessmentId,
            @RequestParam Integer userRef) {
        try {
            Map<String, Object> attemptData = assessmentAttemptService.getAssessmentAttemptData(assessmentId, userRef);
            return new ResponseEntity<>(attemptData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get assessment structure with sections and questions for attempt
     */
    @GetMapping("/{assessmentId}/structure")
    public ResponseEntity<Map<String, Object>> getAssessmentStructure(@PathVariable Long assessmentId) {
        try {
            Map<String, Object> structure = assessmentAttemptService.getAssessmentStructure(assessmentId);
            return new ResponseEntity<>(structure, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Submit assessment answers
     */
    @PostMapping("/{assessmentId}/submit")
    public ResponseEntity<Map<String, Object>> submitAssessment(
            @PathVariable Long assessmentId,
            @RequestParam Integer userRef,
            @RequestBody Map<String, Object> submissionData) {
        try {
            Map<String, Object> result = assessmentAttemptService.submitAssessment(assessmentId, userRef, submissionData);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Update candidate progress during assessment
     */
    @PutMapping("/{assessmentId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long assessmentId,
            @RequestParam Integer userRef,
            @RequestBody Map<String, Object> progressData) {
        try {
            Map<String, Object> result = assessmentAttemptService.updateProgress(assessmentId, userRef, progressData);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get assessment results and analytics for a candidate
     */
    @GetMapping("/{assessmentId}/results")
    public ResponseEntity<Map<String, Object>> getAssessmentResults(
            @PathVariable Long assessmentId,
            @RequestParam Integer userRef) {
        try {
            Map<String, Object> result = assessmentAttemptService.getAssessmentResults(assessmentId, userRef);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
