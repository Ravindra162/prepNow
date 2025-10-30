package com.Assessment.AssessmentService.controller;

import com.Assessment.AssessmentService.dto.AssessmentDto;
import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.mapper.AssessmentMapper;
import com.Assessment.AssessmentService.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AssessmentController {
    
    private final AssessmentService assessmentService;
    private final AssessmentMapper assessmentMapper;

    @PostMapping("/companies/{companyId}/assessments")
    public ResponseEntity<AssessmentDto> createAssessment(@PathVariable Long companyId, @RequestBody Assessment assessment) {
        Assessment createdAssessment = assessmentService.createAssessment(companyId, assessment);
        AssessmentDto dto = assessmentMapper.toDto(createdAssessment);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
    
    @GetMapping("/companies/{companyId}/assessments")
    public ResponseEntity<List<AssessmentDto>> getAssessmentsByCompany(@PathVariable Long companyId) {
        List<Assessment> assessments = assessmentService.getAssessmentsByCompany(companyId);
        List<AssessmentDto> dtos = assessmentMapper.toDtoList(assessments);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
    
    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<AssessmentDto> getAssessmentById(@PathVariable Long assessmentId) {
        return assessmentService.getAssessmentById(assessmentId)
                .map(assessment -> {
                    AssessmentDto dto = assessmentMapper.toDto(assessment);
                    return new ResponseEntity<>(dto, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/assessments/{assessmentId}")
    public ResponseEntity<AssessmentDto> updateAssessment(@PathVariable Long assessmentId, @RequestBody Assessment assessment) {
        Assessment updatedAssessment = assessmentService.updateAssessment(assessmentId, assessment);
        AssessmentDto dto = assessmentMapper.toDto(updatedAssessment);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    
    @DeleteMapping("/assessments/{assessmentId}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long assessmentId) {
        assessmentService.deleteAssessment(assessmentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
