package com.Assessment.AssessmentService.service;

import com.Assessment.AssessmentService.entity.Assessment;
import com.Assessment.AssessmentService.entity.AssessmentCandidate;
import com.Assessment.AssessmentService.exception.ResourceNotFoundException;
import com.Assessment.AssessmentService.repository.AssessmentCandidateRepository;
import com.Assessment.AssessmentService.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentAttemptService {
    
    private final AssessmentRepository assessmentRepository;
    private final AssessmentCandidateRepository assessmentCandidateRepository;
    private final RestTemplate restTemplate;
    
    private static final String QUESTION_SERVICE_URL = "http://localhost:8082";
    
    /**
     * Start an assessment attempt for a candidate
     */
    public Map<String, Object> startAssessmentAttempt(Long assessmentId, Integer userRef) {
        // Get assessment
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));
        
        // Get or create assessment candidate
        AssessmentCandidate candidate = assessmentCandidateRepository
                .findByAssessmentAssessmentIdAndUserRef(assessmentId, userRef)
                .orElseGet(() -> {
                    // Create new candidate record for analytics purposes
                    AssessmentCandidate newCandidate = new AssessmentCandidate();
                    newCandidate.setAssessment(assessment);
                    newCandidate.setUserRef(userRef);
                    newCandidate.setStatus(AssessmentCandidate.CandidateStatus.IN_PROGRESS);
                    newCandidate.setStartedAt(java.time.LocalDateTime.now());
                    
                    // Initialize analytics fields
                    newCandidate.setTimeRemainingMinutes(assessment.getDurationMinutes());
                    
                    return assessmentCandidateRepository.save(newCandidate);
                });
        
        // Update candidate status to IN_PROGRESS if not already and set start time
        if (candidate.getStatus() != AssessmentCandidate.CandidateStatus.IN_PROGRESS) {
            candidate.setStatus(AssessmentCandidate.CandidateStatus.IN_PROGRESS);
            if (candidate.getStartedAt() == null) {
                candidate.setStartedAt(java.time.LocalDateTime.now());
            }
            assessmentCandidateRepository.save(candidate);
        }
        
        // Return assessment data without calling getAssessmentAttemptData to avoid infinite loops
        Map<String, Object> structure = getAssessmentStructure(assessmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("assessment", assessment);
        result.put("candidate", candidate);
        result.put("sections", structure.get("sections"));
        result.put("questionsMap", structure.get("questionsMap"));
        
        return result;
    }
    
    /**
     * Get assessment attempt data including sections and questions
     */
    public Map<String, Object> getAssessmentAttemptData(Long assessmentId, Integer userRef) {
        // Get assessment
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));
        
        // Get existing assessment candidate (don't create new one here)
        AssessmentCandidate candidate = assessmentCandidateRepository
                .findByAssessmentAssessmentIdAndUserRef(assessmentId, userRef)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment attempt not started. Please start the assessment first."));
        
        // Get assessment structure with sections and questions
        Map<String, Object> structure = getAssessmentStructure(assessmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("assessment", assessment);
        result.put("candidate", candidate);
        result.put("sections", structure.get("sections"));
        result.put("questionsMap", structure.get("questionsMap"));
        
        return result;
    }
    
    /**
     * Get assessment structure with sections and questions
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAssessmentStructure(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));
        
        Map<String, Object> structure = assessment.getStructure();
        if (structure == null || structure.isEmpty()) {
            throw new RuntimeException("Assessment structure is not configured");
        }
        
        List<Map<String, Object>> sections = new ArrayList<>();
        Map<Long, List<Map<String, Object>>> questionsMap = new HashMap<>();
        
        try {
            // Parse the assessment structure
            Object sectionsObj = structure.get("sections");
            
            if (sectionsObj instanceof List) {
                List<?> sectionsList = (List<?>) sectionsObj;
                
                for (Object sectionItem : sectionsList) {
                    if (sectionItem instanceof Map) {
                        // New format: sections contain full section data with questions
                        Map<String, Object> sectionData = (Map<String, Object>) sectionItem;
                        Long sectionId = Long.valueOf(sectionData.get("sectionId").toString());
                        
                        // Fetch section details from Question Service
                        try {
                            String sectionUrl = QUESTION_SERVICE_URL + "/sections/" + sectionId;
                            Map<String, Object> sectionDetails = restTemplate.getForObject(sectionUrl, Map.class);
                            
                            if (sectionDetails != null) {
                                sections.add(sectionDetails);
                                
                                // Get questions for this section from the structure
                                Object questionsObj = sectionData.get("questions");
                                List<Map<String, Object>> sectionQuestions = new ArrayList<>();
                                
                                if (questionsObj instanceof List) {
                                    List<Map<String, Object>> questionsList = (List<Map<String, Object>>) questionsObj;
                                    
                                    for (Map<String, Object> questionData : questionsList) {
                                        Long questionId = Long.valueOf(questionData.get("questionId").toString());
                                        
                                        try {
                                            String questionUrl = QUESTION_SERVICE_URL + "/questions/" + questionId;
                                            Map<String, Object> questionDetails = restTemplate.getForObject(questionUrl, Map.class);
                                            
                                            if (questionDetails != null) {
                                                // Fetch MCQ options or test cases based on question type
                                                String questionType = (String) questionDetails.get("type");
                                                
                                                if ("MCQ".equals(questionType)) {
                                                    String mcqUrl = QUESTION_SERVICE_URL + "/questions/" + questionId + "/mcq-options";
                                                    try {
                                                        List<Map<String, Object>> mcqOptions = restTemplate.getForObject(mcqUrl, List.class);
                                                        questionDetails.put("mcqOptions", mcqOptions != null ? mcqOptions : new ArrayList<>());
                                                    } catch (Exception e) {
                                                        log.warn("Could not fetch MCQ options for question {}: {}", questionId, e.getMessage());
                                                        questionDetails.put("mcqOptions", new ArrayList<>());
                                                    }
                                                } else if ("CODING".equals(questionType)) {
                                                    String testCasesUrl = QUESTION_SERVICE_URL + "/questions/" + questionId + "/test-cases/sample";
                                                    try {
                                                        List<Map<String, Object>> testCases = restTemplate.getForObject(testCasesUrl, List.class);
                                                        questionDetails.put("testCases", testCases != null ? testCases : new ArrayList<>());
                                                    } catch (Exception e) {
                                                        log.warn("Could not fetch test cases for question {}: {}", questionId, e.getMessage());
                                                        questionDetails.put("testCases", new ArrayList<>());
                                                    }
                                                }
                                                
                                                sectionQuestions.add(questionDetails);
                                            }
                                        } catch (Exception e) {
                                            log.error("Error fetching question {}: {}", questionId, e.getMessage());
                                        }
                                    }
                                }
                                
                                questionsMap.put(sectionId, sectionQuestions);
                            }
                        } catch (Exception e) {
                            log.error("Error fetching section {}: {}", sectionId, e.getMessage());
                        }
                    } else if (sectionItem instanceof String) {
                        // Legacy format: sections are just strings/names, fetch all questions from that section
                        String sectionName = (String) sectionItem;
                        log.info("Processing legacy format section name: {}", sectionName);
                        
                        // Map legacy section names to actual section names
                        String actualSectionName = mapLegacySectionName(sectionName);
                        log.info("Mapped legacy section '{}' to actual section '{}'", sectionName, actualSectionName);
                        
                        // For now, let's try to fetch all sections and find matching one
                        try {
                            String allSectionsUrl = QUESTION_SERVICE_URL + "/sections";
                            List<Map<String, Object>> allSections = restTemplate.getForObject(allSectionsUrl, List.class);
                            
                            if (allSections != null) {
                                for (Map<String, Object> section : allSections) {
                                    String name = (String) section.get("name");
                                    if (actualSectionName.equalsIgnoreCase(name)) {
                                        Long sectionId = Long.valueOf(section.get("sectionId").toString());
                                        sections.add(section);
                                        log.info("Found matching section: {} with ID: {}", name, sectionId);
                                        
                                        // Fetch all questions for this section
                                        try {
                                            String questionsUrl = QUESTION_SERVICE_URL + "/questions/sections/" + sectionId;
                                            List<Map<String, Object>> sectionQuestions = restTemplate.getForObject(questionsUrl, List.class);
                                            
                                            if (sectionQuestions != null && !sectionQuestions.isEmpty()) {
                                                // Fetch MCQ options and test cases for each question
                                                for (Map<String, Object> question : sectionQuestions) {
                                                    Long questionId = Long.valueOf(question.get("questionId").toString());
                                                    String questionType = (String) question.get("type");
                                                    
                                                    if ("MCQ".equals(questionType)) {
                                                        String mcqUrl = QUESTION_SERVICE_URL + "/questions/" + questionId + "/mcq-options";
                                                        try {
                                                            List<Map<String, Object>> mcqOptions = restTemplate.getForObject(mcqUrl, List.class);
                                                            question.put("mcqOptions", mcqOptions != null ? mcqOptions : new ArrayList<>());
                                                        } catch (Exception e) {
                                                            log.warn("Could not fetch MCQ options for question {}: {}", questionId, e.getMessage());
                                                            question.put("mcqOptions", new ArrayList<>());
                                                        }
                                                    } else if ("CODING".equals(questionType)) {
                                                        String testCasesUrl = QUESTION_SERVICE_URL + "/questions/" + questionId + "/test-cases/sample";
                                                        try {
                                                            List<Map<String, Object>> testCases = restTemplate.getForObject(testCasesUrl, List.class);
                                                            question.put("testCases", testCases != null ? testCases : new ArrayList<>());
                                                        } catch (Exception e) {
                                                            log.warn("Could not fetch test cases for question {}: {}", questionId, e.getMessage());
                                                            question.put("testCases", new ArrayList<>());
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            questionsMap.put(sectionId, sectionQuestions != null ? sectionQuestions : new ArrayList<>());
                                            log.info("Added {} questions for section {}", 
                                                sectionQuestions != null ? sectionQuestions.size() : 0, name);
                                        } catch (Exception e) {
                                            log.error("Error fetching questions for section {}: {}", sectionId, e.getMessage());
                                            questionsMap.put(sectionId, new ArrayList<>());
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error fetching sections for legacy format: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing assessment structure: {}", e.getMessage());
            throw new RuntimeException("Invalid assessment structure format");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("sections", sections);
        result.put("questionsMap", questionsMap);
        
        return result;
    }
    
    /**
     * Submit assessment answers and calculate score
     */
    public Map<String, Object> submitAssessment(Long assessmentId, Integer userRef, Map<String, Object> submissionData) {
        // Get assessment and candidate
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));
        
        AssessmentCandidate candidate = assessmentCandidateRepository
                .findByAssessmentAssessmentIdAndUserRef(assessmentId, userRef)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for this assessment"));
        
        // Update candidate with submission data
        candidate.setStatus(AssessmentCandidate.CandidateStatus.COMPLETED);
        candidate.setCompletedAt(java.time.LocalDateTime.now());
        
        // Calculate time taken
        if (candidate.getStartedAt() != null) {
            long timeTakenMinutes = java.time.Duration.between(candidate.getStartedAt(), candidate.getCompletedAt()).toMinutes();
            candidate.setTimeTakenMinutes((int) timeTakenMinutes);
            candidate.setTimeRemainingMinutes(Math.max(0, assessment.getDurationMinutes() - (int) timeTakenMinutes));
        }
        
        // Store answers
        @SuppressWarnings("unchecked")
        Map<String, Object> answers = (Map<String, Object>) submissionData.get("answers");
        candidate.setAnswers(answers);
        
        // Set submission method
        String submissionMethod = (String) submissionData.getOrDefault("submissionMethod", "MANUAL_SUBMIT");
        candidate.setSubmissionMethod(AssessmentCandidate.SubmissionMethod.valueOf(submissionMethod));
        
        // Store browser info and IP if provided
        candidate.setBrowserInfo((String) submissionData.get("browserInfo"));
        candidate.setIpAddress((String) submissionData.get("ipAddress"));
        
        // TODO: Calculate scores based on correct answers (will implement later)
        // For now, just initialize with basic analytics
        initializeBasicAnalytics(candidate, answers);
        
        // Save updated candidate
        assessmentCandidateRepository.save(candidate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("candidate", candidate);
        result.put("assessment", assessment);
        result.put("submissionTime", candidate.getCompletedAt());
        result.put("status", "submitted");
        
        return result;
    }
    
    /**
     * Update candidate progress during assessment
     */
    public Map<String, Object> updateProgress(Long assessmentId, Integer userRef, Map<String, Object> progressData) {
        AssessmentCandidate candidate = assessmentCandidateRepository
                .findByAssessmentAssessmentIdAndUserRef(assessmentId, userRef)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for this assessment"));
        
        // Update time remaining if provided
        Integer timeRemaining = (Integer) progressData.get("timeRemainingMinutes");
        if (timeRemaining != null) {
            candidate.setTimeRemainingMinutes(timeRemaining);
        }
        
        // Update answers if provided (for auto-save)
        @SuppressWarnings("unchecked")
        Map<String, Object> answers = (Map<String, Object>) progressData.get("answers");
        if (answers != null) {
            candidate.setAnswers(answers);
        }
        
        // Update attempted questions count if provided
        Integer attempted = (Integer) progressData.get("attemptedQuestions");
        if (attempted != null) {
            candidate.setAttemptedQuestions(attempted);
        }
        
        assessmentCandidateRepository.save(candidate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("candidate", candidate);
        result.put("status", "progress_updated");
        
        return result;
    }
    
    /**
     * Get candidate assessment results and analytics
     */
    public Map<String, Object> getAssessmentResults(Long assessmentId, Integer userRef) {
        AssessmentCandidate candidate = assessmentCandidateRepository
                .findByAssessmentAssessmentIdAndUserRef(assessmentId, userRef)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for this assessment"));
        
        Assessment assessment = candidate.getAssessment();
        
        Map<String, Object> result = new HashMap<>();
        result.put("candidate", candidate);
        result.put("assessment", assessment);
        result.put("score", candidate.getTotalScore());
        result.put("percentage", candidate.getPercentageScore());
        result.put("isPassed", candidate.getIsPassed());
        result.put("timeTaken", candidate.getTimeTakenMinutes());
        result.put("analytics", buildAnalyticsReport(candidate));
        
        return result;
    }
    
    /**
     * Initialize basic analytics for a candidate
     */
    private void initializeBasicAnalytics(AssessmentCandidate candidate, Map<String, Object> answers) {
        if (answers != null) {
            candidate.setAttemptedQuestions(answers.size());
            // More detailed analytics will be implemented when we add answer validation
        }
    }
    
    /**
     * Build analytics report for a candidate
     */
    private Map<String, Object> buildAnalyticsReport(AssessmentCandidate candidate) {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("totalQuestions", candidate.getTotalQuestions());
        analytics.put("attemptedQuestions", candidate.getAttemptedQuestions());
        analytics.put("correctAnswers", candidate.getCorrectAnswers());
        analytics.put("incorrectAnswers", candidate.getIncorrectAnswers());
        analytics.put("unansweredQuestions", candidate.getUnansweredQuestions());
        
        // Question type breakdown
        Map<String, Object> questionTypeBreakdown = new HashMap<>();
        questionTypeBreakdown.put("mcq", Map.of(
            "attempted", candidate.getMcqAttempted() != null ? candidate.getMcqAttempted() : 0,
            "correct", candidate.getMcqCorrect() != null ? candidate.getMcqCorrect() : 0
        ));
        questionTypeBreakdown.put("coding", Map.of(
            "attempted", candidate.getCodingAttempted() != null ? candidate.getCodingAttempted() : 0,
            "passed", candidate.getCodingPassed() != null ? candidate.getCodingPassed() : 0
        ));
        analytics.put("questionTypeBreakdown", questionTypeBreakdown);
        
        // Difficulty level breakdown
        Map<String, Object> difficultyBreakdown = new HashMap<>();
        difficultyBreakdown.put("easy", Map.of(
            "attempted", candidate.getEasyAttempted() != null ? candidate.getEasyAttempted() : 0,
            "correct", candidate.getEasyCorrect() != null ? candidate.getEasyCorrect() : 0
        ));
        difficultyBreakdown.put("medium", Map.of(
            "attempted", candidate.getMediumAttempted() != null ? candidate.getMediumAttempted() : 0,
            "correct", candidate.getMediumCorrect() != null ? candidate.getMediumCorrect() : 0
        ));
        difficultyBreakdown.put("hard", Map.of(
            "attempted", candidate.getHardAttempted() != null ? candidate.getHardAttempted() : 0,
            "correct", candidate.getHardCorrect() != null ? candidate.getHardCorrect() : 0
        ));
        analytics.put("difficultyBreakdown", difficultyBreakdown);
        
        analytics.put("sectionScores", candidate.getSectionScores());
        
        return analytics;
    }
    
    /**
     * Map legacy section names to actual section names
     */
    private String mapLegacySectionName(String legacyName) {
        switch (legacyName.toLowerCase()) {
            case "sec_dsa":
            case "dsa":
            case "data_structures":
                return "Data Structures and Algorithms";
            case "sec_cs_fundamentals":
            case "cs_fundamentals":
            case "cs":
                return "CS Fundamentals";
            case "sec_aptitude":
            case "aptitude":
                return "Aptitude";
            case "sec_basics":
            case "basics":
                return "Basics";
            case "sec_javascript":
            case "javascript":
                return "JavaScript Basics";
            case "sec_mental_math":
            case "mental_math":
                return "Mental Math";
            default:
                // Return the original name if no mapping found
                return legacyName;
        }
    }
}
