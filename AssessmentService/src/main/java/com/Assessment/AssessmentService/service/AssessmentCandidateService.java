package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.dto.UpdateScoreRequest;
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

    /**
     * Update candidate score after evaluation
     */
    public AssessmentCandidate updateCandidateScore(Long candidateId, UpdateScoreRequest scoreRequest) {
        AssessmentCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        // Update scores
        candidate.setTotalScore(scoreRequest.getTotalScore());
        candidate.setMaxScore(scoreRequest.getMaxScore());
        candidate.setPercentageScore(scoreRequest.getPercentageScore());
        candidate.setIsPassed(scoreRequest.getIsPassed());

        // Update statistics
        candidate.setTotalQuestions(scoreRequest.getTotalQuestions());
        candidate.setAttemptedQuestions(scoreRequest.getAttemptedQuestions());
        candidate.setCorrectAnswers(scoreRequest.getCorrectAnswers());
        candidate.setIncorrectAnswers(scoreRequest.getIncorrectAnswers());
        candidate.setUnansweredQuestions(scoreRequest.getUnansweredQuestions());

        // Update MCQ and Coding stats
        candidate.setMcqCorrect(scoreRequest.getMcqCorrect());
        candidate.setMcqAttempted(scoreRequest.getMcqTotal());
        candidate.setCodingPassed(scoreRequest.getCodingPassed());
        candidate.setCodingAttempted(scoreRequest.getCodingTotal());

        // Update status to EVALUATED
        candidate.setStatus(AssessmentCandidate.CandidateStatus.EVALUATED);

        return candidateRepository.save(candidate);
    }

    /**
     * Get all candidates/assessments for a specific user
     */
    @Transactional(readOnly = true)
    public List<AssessmentCandidate> getCandidatesByUser(Integer userRef) {
        List<AssessmentCandidate> candidates = candidateRepository.findByUserRefOrderByCreatedAtDesc(userRef);

        // Explicitly load the assessment and company to avoid lazy loading issues
        // Also populate denormalized fields if they are missing
        candidates.forEach(candidate -> {
            if (candidate.getAssessment() != null) {
                Assessment assessment = candidate.getAssessment();
                // Trigger lazy load
                String assessmentName = assessment.getName();

                // Populate denormalized fields if missing
                boolean needsUpdate = false;
                if (candidate.getAssessmentName() == null) {
                    candidate.setAssessmentName(formatAssessmentName(assessmentName));
                    needsUpdate = true;
                }

                if (assessment.getCompany() != null) {
                    // Trigger lazy load for company
                    String companyName = assessment.getCompany().getName();
                    if (candidate.getCompanyName() == null) {
                        candidate.setCompanyName(companyName);
                        needsUpdate = true;
                    }
                }

                // Save if we updated any denormalized fields
                if (needsUpdate) {
                    candidateRepository.save(candidate);
                }
            }
        });

        return candidates;
    }

    /**
     * Format assessment name from snake_case or database format to human-readable format
     * Example: "test_1_assessment" -> "Test 1 Assessment"
     */
    private String formatAssessmentName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Replace underscores with spaces
        String formatted = name.replace("_", " ");

        // Capitalize each word
        String[] words = formatted.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                // Capitalize first letter of each word
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }

        return result.toString();
    }
}
