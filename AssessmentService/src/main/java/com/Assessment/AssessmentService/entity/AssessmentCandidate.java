package com.Assessment.AssessmentService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assessment_candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentCandidate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    
    @Column(name = "user_ref", nullable = false)
    private Integer userRef;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateStatus status = CandidateStatus.INVITED;
    
    public enum CandidateStatus {
        INVITED, IN_PROGRESS, COMPLETED, EVALUATED
    }
}
