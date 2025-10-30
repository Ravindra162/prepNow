package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.dto.UpdateScoreRequest;
import com.Assessment.AssessmentService.entity.AssessmentCandidate;
import com.Assessment.AssessmentService.service.AssessmentCandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AssessmentCandidateController {
    
    private final AssessmentCandidateService candidateService;
    
    @PostMapping("/assessments/{assessmentId}/candidates")
    public ResponseEntity<AssessmentCandidate> addCandidate(@PathVariable Long assessmentId, @RequestBody AssessmentCandidate candidate) {
        AssessmentCandidate addedCandidate = candidateService.addCandidate(assessmentId, candidate);
        return new ResponseEntity<>(addedCandidate, HttpStatus.CREATED);
    }
    
    @GetMapping("/assessments/{assessmentId}/candidates")
    public ResponseEntity<List<AssessmentCandidate>> getCandidatesByAssessment(@PathVariable Long assessmentId) {
        List<AssessmentCandidate> candidates = candidateService.getCandidatesByAssessment(assessmentId);
        return new ResponseEntity<>(candidates, HttpStatus.OK);
    }
    
    @PutMapping("/assessments/{assessmentId}/candidates/{candidateId}")
    public ResponseEntity<AssessmentCandidate> updateCandidate(@PathVariable Long assessmentId, 
                                                              @PathVariable Long candidateId, 
                                                              @RequestBody AssessmentCandidate candidate) {
        AssessmentCandidate updatedCandidate = candidateService.updateCandidate(assessmentId, candidateId, candidate);
        return new ResponseEntity<>(updatedCandidate, HttpStatus.OK);
    }
    
    @DeleteMapping("/assessments/{assessmentId}/candidates/{candidateId}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long assessmentId, @PathVariable Long candidateId) {
        candidateService.deleteCandidate(assessmentId, candidateId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Update candidate score after evaluation
     * Called by SubmissionService after evaluation is complete
     */
    @PutMapping("/assessments/candidates/{candidateId}/score")
    public ResponseEntity<Map<String, Object>> updateCandidateScore(
            @PathVariable Long candidateId,
            @RequestBody UpdateScoreRequest scoreRequest) {
        try {
            AssessmentCandidate candidate = candidateService.updateCandidateScore(candidateId, scoreRequest);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Score updated successfully",
                    "candidate", candidate
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all assessments attempted by a user
     * Used for user dashboard
     */
    @GetMapping("/assessments/candidates/user/{userRef}")
    public ResponseEntity<List<AssessmentCandidate>> getCandidatesByUser(@PathVariable Integer userRef) {
        List<AssessmentCandidate> candidates = candidateService.getCandidatesByUser(userRef);
        return new ResponseEntity<>(candidates, HttpStatus.OK);
    }
}
