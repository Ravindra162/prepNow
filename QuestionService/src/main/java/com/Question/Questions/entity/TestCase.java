package com.Question.Questions.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_case_id")
    private Long testCaseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"mcqOptions", "testCases", "section"})
    private Question question;
    
    @Column(name = "input_data", nullable = false, columnDefinition = "TEXT")
    private String inputData;
    
    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;
    
    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = false; // true for sample test cases visible to candidates
    
    @Column(name = "test_case_order")
    private Integer testCaseOrder;
    
    @Column(columnDefinition = "TEXT")
    private String description; // Optional description for the test case
}
