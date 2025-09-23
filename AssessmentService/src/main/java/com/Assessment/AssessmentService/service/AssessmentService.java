package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.entity.Company;
import com.Assessment.AssessmentService.exception.ResourceNotFoundException;
import com.Assessment.AssessmentService.repository.AssessmentRepository;
import com.Assessment.AssessmentService.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentService {
    
    private final AssessmentRepository assessmentRepository;
    private final CompanyRepository companyRepository;
    
    public Assessment createAssessment(Long companyId, Assessment assessment) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        
        assessment.setCompany(company);
        return assessmentRepository.save(assessment);
    }
    
    @Transactional(readOnly = true)
    public List<Assessment> getAssessmentsByCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
        return assessmentRepository.findByCompanyCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public Optional<Assessment> getAssessmentById(Long assessmentId) {
        return assessmentRepository.findById(assessmentId);
    }
    
    public Assessment updateAssessment(Long assessmentId, Assessment updatedAssessment) {
        Assessment existingAssessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        
        existingAssessment.setName(updatedAssessment.getName());
        existingAssessment.setDescription(updatedAssessment.getDescription());
        existingAssessment.setCreatedBy(updatedAssessment.getCreatedBy());
        existingAssessment.setScheduledAt(updatedAssessment.getScheduledAt());
        existingAssessment.setDurationMinutes(updatedAssessment.getDurationMinutes());
        existingAssessment.setStructure(updatedAssessment.getStructure());
        
        return assessmentRepository.save(existingAssessment);
    }
    
    public void deleteAssessment(Long assessmentId) {
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new ResourceNotFoundException("Assessment not found with id: " + assessmentId);
        }
        assessmentRepository.deleteById(assessmentId);
    }
}
