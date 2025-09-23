package com.Question.Questions.dto;

import com.Question.Questions.entity.Question;
import com.Question.Questions.entity.MCQOption;
import com.Question.Questions.entity.TestCase;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

@Data
public class QuestionCreateRequest {
    private String questionText;
    private String questionType;  // Frontend sends this
    private String difficultyLevel;
    private Integer points;
    private Integer timeLimitMinutes;
    private String codeTemplate;
    private String programmingLanguage;
    
    // Add fields for MCQ options and test cases
    private List<MCQOptionRequest> mcqOptions;
    private List<TestCaseRequest> testCases;

    @Data
    public static class MCQOptionRequest {
        private String optionText;
        private Boolean isCorrect;
        private String optionLabel; // A, B, C, D
        private Integer displayOrder;
    }

    @Data
    public static class TestCaseRequest {
        private String input;
        private String expectedOutput;
        private Boolean isHidden;
    }

    public Question toQuestion() {
        Question question = new Question();
        question.setQuestionText(this.questionText);
        
        // Map questionType string to enum
        if (this.questionType != null) {
            question.setType(Question.QuestionType.valueOf(this.questionType.toUpperCase()));
        }
        
        // Map difficultyLevel string to enum
        if (this.difficultyLevel != null) {
            question.setDifficultyLevel(Question.DifficultyLevel.valueOf(this.difficultyLevel.toUpperCase()));
        }
        
        question.setPoints(this.points);
        question.setTimeLimitMinutes(this.timeLimitMinutes);
        question.setCodeTemplate(this.codeTemplate);
        question.setProgrammingLanguage(this.programmingLanguage);
        
        // Convert MCQ options
        if (this.mcqOptions != null && !this.mcqOptions.isEmpty()) {
            System.out.println("Converting " + this.mcqOptions.size() + " MCQ options");
            List<MCQOption> options = IntStream.range(0, this.mcqOptions.size())
                    .mapToObj(i -> {
                        MCQOptionRequest optionReq = this.mcqOptions.get(i);
                        MCQOption option = new MCQOption();
                        option.setOptionText(optionReq.getOptionText());
                        option.setIsCorrect(optionReq.getIsCorrect() != null ? optionReq.getIsCorrect() : false);

                        // Set option label (A, B, C, D) if not provided
                        option.setOptionLabel(optionReq.getOptionLabel() != null ?
                                optionReq.getOptionLabel() :
                                String.valueOf((char) ('A' + i)));

                        // Set display order
                        option.setDisplayOrder(optionReq.getDisplayOrder() != null ?
                                optionReq.getDisplayOrder() : i + 1);

                        System.out.println("Option " + i + ": " + option.getOptionText() + ", isCorrect: " + option.getIsCorrect());
                        return option;
                    })
                    .toList();
            question.setMcqOptions(options);
        }

        // Convert test cases
        if (this.testCases != null && !this.testCases.isEmpty()) {
            List<TestCase> cases = IntStream.range(0, this.testCases.size())
                    .mapToObj(i -> {
                        TestCaseRequest testCaseReq = this.testCases.get(i);
                        TestCase testCase = new TestCase();
                        testCase.setInputData(testCaseReq.getInput());
                        testCase.setExpectedOutput(testCaseReq.getExpectedOutput());
                        testCase.setIsSample(testCaseReq.getIsHidden() != null ? !testCaseReq.getIsHidden() : true);
                        testCase.setTestCaseOrder(i + 1);
                        return testCase;
                    })
                    .toList();
            question.setTestCases(cases);
        }

        return question;
    }
}
