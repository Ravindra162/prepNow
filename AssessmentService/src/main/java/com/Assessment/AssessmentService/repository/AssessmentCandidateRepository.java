package com.Assessment.AssessmentService.repository;

import com.Assessment.AssessmentService.entity.AssessmentCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentCandidateRepository extends JpaRepository<AssessmentCandidate, Long> {
    List<AssessmentCandidate> findByAssessmentAssessmentId(Long assessmentId);
    Optional<AssessmentCandidate> findByAssessmentAssessmentIdAndUserRef(Long assessmentId, Integer userRef);
    Optional<AssessmentCandidate> findByAssessmentAssessmentIdAndUserRefAndStatus(
            Long assessmentId,
            Integer userRef,
            AssessmentCandidate.CandidateStatus status
    );
    boolean existsByAssessmentAssessmentIdAndUserRef(Long assessmentId, Integer userRef);
    List<AssessmentCandidate> findByUserRefOrderByCreatedAtDesc(Integer userRef);
}
