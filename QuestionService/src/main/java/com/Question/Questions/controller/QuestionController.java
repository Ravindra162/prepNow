package com.Question.Questions.controller;

import com.Question.Questions.entity.Question;
import com.Question.Questions.entity.MCQOption;
import com.Question.Questions.entity.TestCase;
import com.Question.Questions.dto.QuestionCreateRequest;
import com.Question.Questions.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {
    
    private final QuestionService questionService;
    
    @PostMapping("/sections/{sectionId}")
    public ResponseEntity<Question> createQuestion(@PathVariable Long sectionId, @RequestBody QuestionCreateRequest request) {
        try {
            log.info("Creating question for section ID: {}", sectionId);
            log.info("Request data: {}", request);
            
            Question question = request.toQuestion();
            log.info("Converted question - type: {}, difficulty: {}", question.getType(), question.getDifficultyLevel());
            
            Question createdQuestion = questionService.createQuestion(sectionId, question);
            log.info("Question created successfully with ID: {}", createdQuestion.getQuestionId());
            return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating question: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Separate endpoint for adding MCQ options to existing question
    @PostMapping("/{questionId}/mcq-options/batch")
    public ResponseEntity<List<MCQOption>> addMCQOptions(@PathVariable Long questionId, @RequestBody List<MCQOption> options) {
        try {
            log.info("Adding {} MCQ options to question ID: {}", options.size(), questionId);
            List<MCQOption> createdOptions = questionService.addMCQOptions(questionId, options);
            return new ResponseEntity<>(createdOptions, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding MCQ options: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
    
    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<List<Question>> getQuestionsBySection(@PathVariable Long sectionId) {
        List<Question> questions = questionService.getQuestionsBySection(sectionId);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Question>> getQuestionsByType(@PathVariable Question.QuestionType type) {
        List<Question> questions = questionService.getQuestionsByType(type);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
    
    @GetMapping("/{questionId}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long questionId) {
        return questionService.getQuestionById(questionId)
                .map(question -> new ResponseEntity<>(question, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @RequestBody Question question) {
        Question updatedQuestion = questionService.updateQuestion(questionId, question);
        return new ResponseEntity<>(updatedQuestion, HttpStatus.OK);
    }
    
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // MCQ Options endpoints
    @PostMapping("/{questionId}/mcq-options")
    public ResponseEntity<MCQOption> addMCQOption(@PathVariable Long questionId, @RequestBody MCQOption option) {
        try {
            MCQOption createdOption = questionService.addMCQOption(questionId, option);
            return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding MCQ option: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Alternative endpoint for handling direct MCQ options array
    @PostMapping("/{questionId}/mcq-options/array")
    public ResponseEntity<List<MCQOption>> addMCQOptionsFromArray(@PathVariable Long questionId, @RequestBody List<MCQOption> options) {
        try {
            log.info("Adding {} MCQ options from array to question ID: {}", options.size(), questionId);
            List<MCQOption> createdOptions = questionService.addMCQOptions(questionId, options);
            return new ResponseEntity<>(createdOptions, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding MCQ options from array: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{questionId}/mcq-options")
    public ResponseEntity<List<MCQOption>> getMCQOptionsByQuestion(@PathVariable Long questionId) {
        List<MCQOption> options = questionService.getMCQOptionsByQuestion(questionId);
        return new ResponseEntity<>(options, HttpStatus.OK);
    }
    
    @DeleteMapping("/mcq-options/{optionId}")
    public ResponseEntity<Void> deleteMCQOption(@PathVariable Long optionId) {
        questionService.deleteMCQOption(optionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // Test Cases endpoints
    @PostMapping("/{questionId}/test-cases")
    public ResponseEntity<TestCase> addTestCase(@PathVariable Long questionId, @RequestBody TestCase testCase) {
        try {
            TestCase createdTestCase = questionService.addTestCase(questionId, testCase);
            return new ResponseEntity<>(createdTestCase, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding test case: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/{questionId}/test-cases/batch")
    public ResponseEntity<List<TestCase>> addTestCases(@PathVariable Long questionId, @RequestBody List<TestCase> testCases) {
        try {
            log.info("Adding {} test cases to question ID: {}", testCases.size(), questionId);
            List<TestCase> createdTestCases = questionService.addTestCases(questionId, testCases);
            return new ResponseEntity<>(createdTestCases, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding test cases: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{questionId}/test-cases")
    public ResponseEntity<List<TestCase>> getTestCasesByQuestion(@PathVariable Long questionId) {
        List<TestCase> testCases = questionService.getTestCasesByQuestion(questionId);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @GetMapping("/{questionId}/test-cases/sample")
    public ResponseEntity<List<TestCase>> getSampleTestCasesByQuestion(@PathVariable Long questionId) {
        List<TestCase> sampleTestCases = questionService.getSampleTestCasesByQuestion(questionId);
        return new ResponseEntity<>(sampleTestCases, HttpStatus.OK);
    }
    
    @DeleteMapping("/test-cases/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long testCaseId) {
        questionService.deleteTestCase(testCaseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
