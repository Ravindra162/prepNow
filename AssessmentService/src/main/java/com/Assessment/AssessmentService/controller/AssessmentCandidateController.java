package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.entity.AssessmentCandidate;
import com.Assessment.AssessmentService.service.AssessmentCandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
