package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AssessmentController {
    
    private final AssessmentService assessmentService;
    
    @PostMapping("/companies/{companyId}/assessments")
    public ResponseEntity<Assessment> createAssessment(@PathVariable Long companyId, @RequestBody Assessment assessment) {
        Assessment createdAssessment = assessmentService.createAssessment(companyId, assessment);
        return new ResponseEntity<>(createdAssessment, HttpStatus.CREATED);
    }
    
    @GetMapping("/companies/{companyId}/assessments")
    public ResponseEntity<List<Assessment>> getAssessmentsByCompany(@PathVariable Long companyId) {
        List<Assessment> assessments = assessmentService.getAssessmentsByCompany(companyId);
        return new ResponseEntity<>(assessments, HttpStatus.OK);
    }
    
    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long assessmentId) {
        return assessmentService.getAssessmentById(assessmentId)
                .map(assessment -> new ResponseEntity<>(assessment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/assessments/{assessmentId}")
    public ResponseEntity<Assessment> updateAssessment(@PathVariable Long assessmentId, @RequestBody Assessment assessment) {
        Assessment updatedAssessment = assessmentService.updateAssessment(assessmentId, assessment);
        return new ResponseEntity<>(updatedAssessment, HttpStatus.OK);
    }
    
    @DeleteMapping("/assessments/{assessmentId}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long assessmentId) {
        assessmentService.deleteAssessment(assessmentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
