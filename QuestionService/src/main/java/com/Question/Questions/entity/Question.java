package com.Question.Questions.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @JsonIgnoreProperties({"questions"})
    private Section section;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;
    
    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "points")
    private Integer points;
    
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;
    
    // For coding questions
    @Column(columnDefinition = "TEXT")
    private String codeTemplate;
    
    @Column(name = "programming_language")
    private String programmingLanguage;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MCQOption> mcqOptions;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TestCase> testCases;
    
    // Helper methods to maintain bidirectional relationships
    public void addMcqOption(MCQOption option) {
        if (mcqOptions == null) {
            mcqOptions = new java.util.ArrayList<>();
        }
        mcqOptions.add(option);
        option.setQuestion(this);
    }
    
    public void removeMcqOption(MCQOption option) {
        if (mcqOptions != null) {
            mcqOptions.remove(option);
            option.setQuestion(null);
        }
    }
    
    public void addTestCase(TestCase testCase) {
        if (testCases == null) {
            testCases = new java.util.ArrayList<>();
        }
        testCases.add(testCase);
        testCase.setQuestion(this);
    }
    
    public void removeTestCase(TestCase testCase) {
        if (testCases != null) {
            testCases.remove(testCase);
            testCase.setQuestion(null);
        }
    }
    
    public enum QuestionType {
        MCQ, CODING
    }
    
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}
