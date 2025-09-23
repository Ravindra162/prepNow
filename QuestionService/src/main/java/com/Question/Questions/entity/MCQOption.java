package com.Question.Questions.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mcq_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCQOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"mcqOptions", "testCases", "section"})
    private Question question;
    
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;
    
    @Column(name = "option_label", nullable = false)
    private String optionLabel; // A, B, C, D
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;
    
    @Column(name = "display_order")
    private Integer displayOrder;
}
