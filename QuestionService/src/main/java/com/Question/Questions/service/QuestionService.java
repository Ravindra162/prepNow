package com.Question.Questions.service;

import com.Question.Questions.entity.Question;
import com.Question.Questions.entity.Section;
import com.Question.Questions.entity.MCQOption;
import com.Question.Questions.entity.TestCase;
import com.Question.Questions.exception.ResourceNotFoundException;
import com.Question.Questions.repository.QuestionRepository;
import com.Question.Questions.repository.SectionRepository;
import com.Question.Questions.repository.MCQOptionRepository;
import com.Question.Questions.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final SectionRepository sectionRepository;
    private final MCQOptionRepository mcqOptionRepository;
    private final TestCaseRepository testCaseRepository;
    
    public Question createQuestion(Long sectionId, Question question) {
        try {
            log.info("Creating question for section ID: {}", sectionId);
            log.info("Question details - text: {}, type: {}, difficulty: {}", 
                question.getQuestionText(), question.getType(), question.getDifficultyLevel());
            
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));
            
            question.setSection(section);
            log.info("Section set successfully: {}", section.getName());
            
            // Handle MCQ options if present
            if (question.getType() == Question.QuestionType.MCQ && question.getMcqOptions() != null && !question.getMcqOptions().isEmpty()) {
                log.info("Setting up {} MCQ options", question.getMcqOptions().size());
                List<MCQOption> options = question.getMcqOptions();
                // Clear the original list to avoid issues
                question.setMcqOptions(new java.util.ArrayList<>());
                
                for (int i = 0; i < options.size(); i++) {
                    MCQOption option = options.get(i);
                    
                    // Set display order if not provided
                    if (option.getDisplayOrder() == null) {
                        option.setDisplayOrder(i + 1);
                    }
                    
                    // Set option label if not provided
                    if (option.getOptionLabel() == null) {
                        option.setOptionLabel(String.valueOf((char) ('A' + i)));
                    }
                    
                    // Ensure isCorrect is not null
                    if (option.getIsCorrect() == null) {
                        option.setIsCorrect(false);
                    }
                    
                    // Use helper method to maintain bidirectional relationship
                    question.addMcqOption(option);
                    log.info("Added MCQ option: {}, isCorrect: {}", option.getOptionText(), option.getIsCorrect());
                }
            }
            
            // Handle test cases if present
            if (question.getType() == Question.QuestionType.CODING && question.getTestCases() != null && !question.getTestCases().isEmpty()) {
                log.info("Setting up {} test cases", question.getTestCases().size());
                List<TestCase> testCases = question.getTestCases();
                // Clear the original list to avoid issues
                question.setTestCases(new java.util.ArrayList<>());
                
                for (int i = 0; i < testCases.size(); i++) {
                    TestCase testCase = testCases.get(i);
                    
                    // Set test case order if not provided
                    if (testCase.getTestCaseOrder() == null) {
                        testCase.setTestCaseOrder(i + 1);
                    }
                    
                    // Set default isSample if not provided
                    if (testCase.getIsSample() == null) {
                        testCase.setIsSample(false);
                    }
                    
                    // Use helper method to maintain bidirectional relationship
                    question.addTestCase(testCase);
                    log.info("Added test case: input={}, output={}", testCase.getInputData(), testCase.getExpectedOutput());
                }
            }
            
            // Save the question with all its related entities (cascade will handle the rest)
            Question savedQuestion = questionRepository.save(question);
            log.info("Question saved with ID: {}", savedQuestion.getQuestionId());
            
            log.info("Question creation completed successfully");
            return savedQuestion;
        } catch (Exception e) {
            log.error("Error creating question: ", e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsBySection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section not found with id: " + sectionId);
        }
        return questionRepository.findBySectionSectionId(sectionId);
    }
    
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByType(Question.QuestionType type) {
        return questionRepository.findByType(type);
    }
    
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }
    
    public Question updateQuestion(Long questionId, Question updatedQuestion) {
        Question existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        existingQuestion.setQuestionText(updatedQuestion.getQuestionText());
        existingQuestion.setType(updatedQuestion.getType());
        existingQuestion.setDifficultyLevel(updatedQuestion.getDifficultyLevel());
        existingQuestion.setPoints(updatedQuestion.getPoints());
        existingQuestion.setTimeLimitMinutes(updatedQuestion.getTimeLimitMinutes());
        existingQuestion.setCodeTemplate(updatedQuestion.getCodeTemplate());
        existingQuestion.setProgrammingLanguage(updatedQuestion.getProgrammingLanguage());
        
        return questionRepository.save(existingQuestion);
    }
    
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }
    
    // MCQ Options management
    public MCQOption addMCQOption(Long questionId, MCQOption option) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        if (question.getType() != Question.QuestionType.MCQ) {
            throw new IllegalArgumentException("Cannot add MCQ options to non-MCQ question");
        }
        
        // Use helper method to maintain bidirectional relationship
        question.addMcqOption(option);
        
        // Set defaults if not provided
        if (option.getDisplayOrder() == null) {
            int nextOrder = question.getMcqOptions() != null ? question.getMcqOptions().size() : 1;
            option.setDisplayOrder(nextOrder);
        }
        
        if (option.getOptionLabel() == null) {
            int labelIndex = (option.getDisplayOrder() != null ? option.getDisplayOrder() : 1) - 1;
            option.setOptionLabel(String.valueOf((char) ('A' + labelIndex)));
        }
        
        if (option.getIsCorrect() == null) {
            option.setIsCorrect(false);
        }
        
        return mcqOptionRepository.save(option);
    }
    
    public List<MCQOption> addMCQOptions(Long questionId, List<MCQOption> options) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        if (question.getType() != Question.QuestionType.MCQ) {
            throw new IllegalArgumentException("Cannot add MCQ options to non-MCQ question");
        }
        
        // Set the question reference and display order for each option
        for (int i = 0; i < options.size(); i++) {
            MCQOption option = options.get(i);
            
            // Use helper method to maintain bidirectional relationship
            question.addMcqOption(option);
            
            // Set display order if not provided
            if (option.getDisplayOrder() == null) {
                option.setDisplayOrder(i + 1);
            }
            
            // Set option label if not provided
            if (option.getOptionLabel() == null) {
                option.setOptionLabel(String.valueOf((char) ('A' + i)));
            }
            
            // Ensure isCorrect is not null
            if (option.getIsCorrect() == null) {
                option.setIsCorrect(false);
            }
        }
        
        return mcqOptionRepository.saveAll(options);
    }
    
    @Transactional(readOnly = true)
    public List<MCQOption> getMCQOptionsByQuestion(Long questionId) {
        return mcqOptionRepository.findByQuestionQuestionIdOrderByDisplayOrderAsc(questionId);
    }
    
    public void deleteMCQOption(Long optionId) {
        if (!mcqOptionRepository.existsById(optionId)) {
            throw new ResourceNotFoundException("MCQ Option not found with id: " + optionId);
        }
        mcqOptionRepository.deleteById(optionId);
    }
    
    // Test Cases management
    public TestCase addTestCase(Long questionId, TestCase testCase) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        if (question.getType() != Question.QuestionType.CODING) {
            throw new IllegalArgumentException("Cannot add test cases to non-coding question");
        }
        
        // Use helper method to maintain bidirectional relationship
        question.addTestCase(testCase);
        
        // Set defaults if not provided
        if (testCase.getTestCaseOrder() == null) {
            int nextOrder = question.getTestCases() != null ? question.getTestCases().size() : 1;
            testCase.setTestCaseOrder(nextOrder);
        }
        
        if (testCase.getIsSample() == null) {
            testCase.setIsSample(false);
        }
        
        return testCaseRepository.save(testCase);
    }
    
    public List<TestCase> addTestCases(Long questionId, List<TestCase> testCases) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        
        if (question.getType() != Question.QuestionType.CODING) {
            throw new IllegalArgumentException("Cannot add test cases to non-coding question");
        }
        
        // Set the question reference and test case order for each test case
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            
            // Use helper method to maintain bidirectional relationship
            question.addTestCase(testCase);
            
            // Set test case order if not provided
            if (testCase.getTestCaseOrder() == null) {
                testCase.setTestCaseOrder(i + 1);
            }
            
            // Set default isSample if not provided
            if (testCase.getIsSample() == null) {
                testCase.setIsSample(false);
            }
        }
        
        return testCaseRepository.saveAll(testCases);
    }
    
    @Transactional(readOnly = true)
    public List<TestCase> getTestCasesByQuestion(Long questionId) {
        return testCaseRepository.findByQuestionQuestionIdOrderByTestCaseOrderAsc(questionId);
    }
    
    @Transactional(readOnly = true)
    public List<TestCase> getSampleTestCasesByQuestion(Long questionId) {
        return testCaseRepository.findByQuestionQuestionIdAndIsSampleTrue(questionId);
    }
    
    public void deleteTestCase(Long testCaseId) {
        if (!testCaseRepository.existsById(testCaseId)) {
            throw new ResourceNotFoundException("Test Case not found with id: " + testCaseId);
        }
        testCaseRepository.deleteById(testCaseId);
    }
}
