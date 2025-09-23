package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.entity.AssessmentCandidate;
import com.Assessment.AssessmentService.exception.DuplicateResourceException;
import com.Assessment.AssessmentService.exception.ResourceNotFoundException;
import com.Assessment.AssessmentService.repository.AssessmentCandidateRepository;
import com.Assessment.AssessmentService.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentCandidateService {
    
    private final AssessmentCandidateRepository candidateRepository;
    private final AssessmentRepository assessmentRepository;
    
    public AssessmentCandidate addCandidate(Long assessmentId, AssessmentCandidate candidate) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));
        
        if (candidateRepository.existsByAssessmentAssessmentIdAndUserRef(assessmentId, candidate.getUserRef())) {
            throw new DuplicateResourceException("Candidate already exists for this assessment");
        }
        
        candidate.setAssessment(assessment);
        return candidateRepository.save(candidate);
    }
    
    @Transactional(readOnly = true)
    public List<AssessmentCandidate> getCandidatesByAssessment(Long assessmentId) {
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new ResourceNotFoundException("Assessment not found with id: " + assessmentId);
        }
        return candidateRepository.findByAssessmentAssessmentId(assessmentId);
    }
    
    public AssessmentCandidate updateCandidate(Long assessmentId, Long candidateId, AssessmentCandidate updatedCandidate) {
        AssessmentCandidate existingCandidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
        
        if (!existingCandidate.getAssessment().getAssessmentId().equals(assessmentId)) {
            throw new ResourceNotFoundException("Candidate does not belong to the specified assessment");
        }
        
        existingCandidate.setStatus(updatedCandidate.getStatus());
        
        return candidateRepository.save(existingCandidate);
    }
    
    public void deleteCandidate(Long assessmentId, Long candidateId) {
        AssessmentCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
        
        if (!candidate.getAssessment().getAssessmentId().equals(assessmentId)) {
            throw new ResourceNotFoundException("Candidate does not belong to the specified assessment");
        }
        
        candidateRepository.deleteById(candidateId);
    }
}
