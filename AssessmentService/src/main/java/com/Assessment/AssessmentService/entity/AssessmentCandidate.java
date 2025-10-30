package com.Assessment.AssessmentService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

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
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Assessment assessment;
    
    @Column(name = "user_ref", nullable = false)
    private Integer userRef;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateStatus status = CandidateStatus.INVITED;
    
    // Denormalized fields for easier access
    @Column(name = "assessment_name")
    private String assessmentName;

    @Column(name = "company_name")
    private String companyName;

    // Assessment timing fields
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_remaining_minutes")
    private Integer timeRemainingMinutes;

    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;

    // Answer and scoring fields
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private Map<String, Object> answers;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "percentage_score")
    private Double percentageScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    // Question statistics
    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "attempted_questions")
    private Integer attemptedQuestions;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "incorrect_answers")
    private Integer incorrectAnswers;

    @Column(name = "unanswered_questions")
    private Integer unansweredQuestions;

    // Question type statistics
    @Column(name = "mcq_attempted")
    private Integer mcqAttempted;

    @Column(name = "mcq_correct")
    private Integer mcqCorrect;

    @Column(name = "coding_attempted")
    private Integer codingAttempted;

    @Column(name = "coding_passed")
    private Integer codingPassed;

    // Difficulty level statistics
    @Column(name = "easy_attempted")
    private Integer easyAttempted;

    @Column(name = "easy_correct")
    private Integer easyCorrect;

    @Column(name = "medium_attempted")
    private Integer mediumAttempted;

    @Column(name = "medium_correct")
    private Integer mediumCorrect;

    @Column(name = "hard_attempted")
    private Integer hardAttempted;

    @Column(name = "hard_correct")
    private Integer hardCorrect;

    // Section scores
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_scores", columnDefinition = "jsonb")
    private Map<String, Object> sectionScores;

    // Submission metadata
    @Column(name = "browser_info")
    private String browserInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_method")
    private SubmissionMethod submissionMethod;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum CandidateStatus {
        INVITED, IN_PROGRESS, COMPLETED, EVALUATED
    }

    public enum SubmissionMethod {
        AUTO_SUBMIT, MANUAL_SUBMIT, TIME_EXPIRED
    }
}
