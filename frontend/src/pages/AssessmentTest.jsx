import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { assessmentService, questionService } from '../utils/adminApi';
import { useAuth } from '../contexts/AuthContext';
import CodingQuestion from '../components/CodingQuestion';
import {
  ClockIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  FlagIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';

const AssessmentTest = () => {
  const { assessmentId } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  // Assessment data
  const [assessment, setAssessment] = useState(null);
  const [sections, setSections] = useState([]);
  const [questions, setQuestions] = useState({});
  const [loading, setLoading] = useState(true);

  // Test state
  const [currentSectionIndex, setCurrentSectionIndex] = useState(0);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [timeRemaining, setTimeRemaining] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchAssessmentData();
  }, [assessmentId]);

  useEffect(() => {
    if (assessment && assessment.durationMinutes) {
      setTimeRemaining(assessment.durationMinutes * 60); // Convert minutes to seconds
    }
  }, [assessment]);

  useEffect(() => {
    if (timeRemaining > 0) {
      const timer = setInterval(() => {
        setTimeRemaining(prev => {
          if (prev <= 1) {
            handleSubmitAssessment();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [timeRemaining]);

  const fetchAssessmentData = async () => {
    try {
      setLoading(true);

      // Fetch assessment details
      const assessmentData = await assessmentService.getAssessmentById(assessmentId);
      setAssessment(assessmentData);

      console.log('Assessment data:', assessmentData);
      console.log('Assessment structure:', assessmentData.structure);

      // Get sections ONLY from assessment structure - no fallback to all sections
      let sectionsData = [];

      if (assessmentData.structure && assessmentData.structure.sections && Array.isArray(assessmentData.structure.sections)) {
        // Handle both string identifiers and object with ID properties
        const sectionIdentifiers = assessmentData.structure.sections.map(s => {
          // If s is a string, use it directly
          if (typeof s === 'string') {
            return s;
          }
          // If s is an object, extract ID
          return s.sectionId || s.id || s.section_id;
        }).filter(id => id !== undefined && id !== null && id !== '');

        console.log('Section identifiers from assessment structure:', sectionIdentifiers);

        if (sectionIdentifiers.length > 0) {
          // Try to fetch sections by ID first, then by name/identifier
          const sectionPromises = sectionIdentifiers.map(async (identifier) => {
            try {
              console.log(`Attempting to fetch section with identifier: ${identifier}`);

              // First try to fetch by ID (if it's numeric)
              if (!isNaN(identifier) && Number.isInteger(Number(identifier))) {
                console.log(`Fetching section by ID: ${identifier}`);
                return await questionService.getSectionById(Number(identifier));
              }

              // If it's a string identifier, try to fetch by ID anyway
              try {
                console.log(`Fetching section by string ID: ${identifier}`);
                const section = await questionService.getSectionById(identifier);
                return section;
              } catch (idError) {
                console.log(`Failed to fetch by ID, trying alternative methods for: ${identifier}`);

                // If fetching by ID fails, we need to fetch all sections and filter
                // This is a fallback for string identifiers that don't map to numeric IDs
                const allSections = await questionService.getSections();
                console.log('All sections:', allSections);

                // Try to match by name, identifier, or other properties
                const matchedSection = allSections.find(section =>
                  section.name === identifier ||
                  section.sectionId === identifier ||
                  section.identifier === identifier ||
                  section.code === identifier
                );

                if (matchedSection) {
                  console.log(`Found matching section:`, matchedSection);
                  return matchedSection;
                } else {
                  console.warn(`No section found matching identifier: ${identifier}`);
                  return null;
                }
              }
            } catch (error) {
              console.error(`Error fetching section ${identifier}:`, error);
              return null;
            }
          });

          const fetchedSections = await Promise.all(sectionPromises);
          sectionsData = fetchedSections.filter(section => section !== null);

          console.log(`Successfully fetched ${sectionsData.length} sections for this assessment`);
          console.log('Fetched sections:', sectionsData);
        } else {
          console.warn('No valid section identifiers found in assessment structure');
        }
      } else {
        console.warn('Assessment structure is missing or invalid - no sections property found');
      }

      // Set sections (will be empty if assessment has no proper structure)
      setSections(sectionsData);

      // Fetch questions for each section that was successfully loaded
      const questionsData = {};
      for (const section of sectionsData) {
        try {
          console.log(`Fetching questions for section: ${section.name} (ID: ${section.sectionId})`);
          const sectionQuestions = await questionService.getQuestionsBySection(section.sectionId);
          questionsData[section.sectionId] = sectionQuestions;
          console.log(`Found ${sectionQuestions.length} questions in section ${section.name}`);
        } catch (error) {
          console.error(`Error fetching questions for section ${section.sectionId}:`, error);
          questionsData[section.sectionId] = [];
        }
      }
      setQuestions(questionsData);

      console.log('Final sections loaded:', sectionsData.length);
      console.log('Final questions data:', questionsData);

    } catch (error) {
      console.error('Error fetching assessment data:', error);
      toast.error('Failed to load assessment');
      navigate('/companies');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (questionId, selectedOption) => {
    setAnswers(prev => ({
      ...prev,
      [questionId]: selectedOption
    }));
  };

  const getCurrentSection = () => {
    return sections[currentSectionIndex];
  };

  const getCurrentQuestions = () => {
    const currentSection = getCurrentSection();
    return currentSection ? questions[currentSection.sectionId] || [] : [];
  };

  const getCurrentQuestion = () => {
    const currentQuestions = getCurrentQuestions();
    return currentQuestions[currentQuestionIndex];
  };

  const goToNextQuestion = () => {
    const currentQuestions = getCurrentQuestions();
    if (currentQuestionIndex < currentQuestions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
    } else {
      // Move to next section
      if (currentSectionIndex < sections.length - 1) {
        setCurrentSectionIndex(prev => prev + 1);
        setCurrentQuestionIndex(0);
      }
    }
  };

  const goToPreviousQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1);
    } else {
      // Move to previous section
      if (currentSectionIndex > 0) {
        setCurrentSectionIndex(prev => prev - 1);
        const prevSection = sections[currentSectionIndex - 1];
        const prevQuestions = questions[prevSection.sectionId] || [];
        setCurrentQuestionIndex(prevQuestions.length - 1);
      }
    }
  };

  const isLastQuestion = () => {
    const currentQuestions = getCurrentQuestions();
    return currentSectionIndex === sections.length - 1 &&
      currentQuestionIndex === currentQuestions.length - 1;
  };

  const isFirstQuestion = () => {
    return currentSectionIndex === 0 && currentQuestionIndex === 0;
  };

  const handleSubmitAssessment = async () => {
    if (isSubmitting) return;

    // Confirm submission
    const confirmSubmit = window.confirm(
      'Are you sure you want to submit? You cannot change your answers after submission.'
    );

    if (!confirmSubmit) return;

    try {
      setIsSubmitting(true);

      // Get user reference (using a simple hash of email for now)
      const userRef = getUserRef();

      // Prepare submission data
      const submissionData = {
        answers: answers,
        submissionMethod: timeRemaining <= 0 ? 'TIME_EXPIRED' : 'MANUAL_SUBMIT',
        browserInfo: navigator.userAgent,
        ipAddress: null, // Will be set by backend if needed
      };

      console.log('Submitting assessment:', submissionData);

      // Submit to backend
      await assessmentService.submitAssessment(assessmentId, userRef, submissionData);

      toast.success('Assessment submitted successfully!');
      navigate('/my-tests');
    } catch (error) {
      console.error('Error submitting assessment:', error);
      toast.error('Failed to submit assessment. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Helper function to get user reference from current user
  const getUserRef = () => {
    if (!currentUser || !currentUser.email) {
      toast.error('User not authenticated');
      navigate('/login');
      return 0;
    }

    // Create a simple numeric hash from email
    let hash = 0;
    for (let i = 0; i < currentUser.email.length; i++) {
      const char = currentUser.email.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32-bit integer
    }
    return Math.abs(hash);
  };

  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading assessment...</p>
        </div>
      </div>
    );
  }

  if (!assessment || sections.length === 0) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">No questions available</h2>
          <button
            onClick={() => navigate('/companies')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Back to Companies
          </button>
        </div>
      </div>
    );
  }

  const currentSection = getCurrentSection();
  const currentQuestions = getCurrentQuestions();
  const currentQuestion = getCurrentQuestion();

  // Check if current question is a coding question
  const isCodingQuestion = currentQuestion?.type === 'CODING';

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div>
              <h1 className="text-lg font-semibold text-gray-900">
                {assessment.name || assessment.title}
              </h1>
              <p className="text-xs text-gray-600">
                Section: {currentSection?.name} - Question {currentQuestionIndex + 1} of {currentQuestions.length}
              </p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center text-sm text-gray-600">
                <ClockIcon className="h-4 w-4 mr-1" />
                <span className={`font-mono ${timeRemaining < 300 ? 'text-red-600' : ''}`}>
                  {formatTime(timeRemaining)}
                </span>
              </div>
              <button
                onClick={handleSubmitAssessment}
                disabled={isSubmitting}
                className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
              >
                {isSubmitting ? 'Submitting...' : 'Submit'}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Render full-screen coding question layout */}
      {isCodingQuestion ? (
        <div className="h-[calc(100vh-64px)]">
          {currentQuestion && (
            <div className="h-full flex flex-col">
              {/* Coding Question Component - Full Height */}
              <div className="flex-1 overflow-hidden">
                <CodingQuestion
                  question={currentQuestion}
                  answer={answers[currentQuestion.questionId] || ''}
                  onAnswerChange={(code) => handleAnswerSelect(currentQuestion.questionId, code)}
                />
              </div>

              {/* Navigation Buttons - Fixed at Bottom */}
              <div className="bg-white border-t border-gray-200 px-6 py-4">
                <div className="max-w-7xl mx-auto flex justify-between items-center">
                  <button
                    onClick={goToPreviousQuestion}
                    disabled={isFirstQuestion()}
                    className="flex items-center px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronLeftIcon className="h-4 w-4 mr-2" />
                    Previous
                  </button>

                  <div className="text-sm text-gray-600">
                    Question {currentQuestionIndex + 1} of {currentQuestions.length} in {currentSection?.name}
                  </div>

                  {isLastQuestion() ? (
                    <button
                      onClick={handleSubmitAssessment}
                      disabled={isSubmitting}
                      className="flex items-center px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                    >
                      <CheckCircleIcon className="h-4 w-4 mr-2" />
                      {isSubmitting ? 'Submitting...' : 'Submit Assessment'}
                    </button>
                  ) : (
                    <button
                      onClick={goToNextQuestion}
                      className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                    >
                      Next
                      <ChevronRightIcon className="h-4 w-4 ml-2" />
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      ) : (
        /* Regular layout for MCQ questions */
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Question Navigation Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-lg shadow-sm p-4">
                <h3 className="font-semibold text-gray-900 mb-4">Sections</h3>
                <div className="space-y-2">
                  {sections.map((section, sectionIdx) => (
                    <div
                      key={section.sectionId}
                      className={`p-3 rounded-md cursor-pointer ${sectionIdx === currentSectionIndex
                        ? 'bg-blue-100 border border-blue-300'
                        : 'hover:bg-gray-50'
                        }`}
                      onClick={() => {
                        setCurrentSectionIndex(sectionIdx);
                        setCurrentQuestionIndex(0);
                      }}
                    >
                      <div className="text-sm font-medium text-gray-900">
                        {section.name}
                      </div>
                      <div className="text-xs text-gray-500">
                        {questions[section.sectionId]?.length || 0} questions
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Main Question Area */}
            <div className="lg:col-span-3">
              <div className="bg-white rounded-lg shadow-sm">
                {/* Question Header */}
                <div className="px-6 py-4 border-b border-gray-200">
                  <div className="flex justify-between items-center">
                    <div>
                      <h2 className="text-lg font-semibold text-gray-900">
                        Section: {currentSection?.name}
                      </h2>
                      <p className="text-sm text-gray-600">
                        Question {currentQuestionIndex + 1} of {currentQuestions.length}
                      </p>
                    </div>
                    <div className="flex items-center">
                      <FlagIcon className="h-5 w-5 text-gray-400" />
                    </div>
                  </div>
                </div>

                {/* Question Content */}
                {currentQuestion ? (
                  <div className="p-6">
                    <div className="mb-6">
                      <h3 className="text-lg font-medium text-gray-900 mb-4">
                        {currentQuestion.questionText}
                      </h3>

                      {/* MCQ Options */}
                      {currentQuestion.type === 'MCQ' && currentQuestion.mcqOptions && (
                        <div className="space-y-3">
                          {currentQuestion.mcqOptions.map((option, optionIdx) => (
                            <label
                              key={optionIdx}
                              className="flex items-center p-3 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50"
                            >
                              <input
                                type="radio"
                                name={`question-${currentQuestion.questionId}`}
                                value={option.optionText}
                                checked={answers[currentQuestion.questionId] === option.optionText}
                                onChange={() => handleAnswerSelect(currentQuestion.questionId, option.optionText)}
                                className="mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500"
                              />
                              <span className="text-gray-900">{option.optionText}</span>
                            </label>
                          ))}
                        </div>
                      )}
                    </div>

                    {/* Navigation Buttons */}
                    <div className="flex justify-between">
                      <button
                        onClick={goToPreviousQuestion}
                        disabled={isFirstQuestion()}
                        className="flex items-center px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <ChevronLeftIcon className="h-4 w-4 mr-2" />
                        Previous
                      </button>

                      {isLastQuestion() ? (
                        <button
                          onClick={handleSubmitAssessment}
                          disabled={isSubmitting}
                          className="flex items-center px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                        >
                          <CheckCircleIcon className="h-4 w-4 mr-2" />
                          {isSubmitting ? 'Submitting...' : 'Submit Assessment'}
                        </button>
                      ) : (
                        <button
                          onClick={goToNextQuestion}
                          className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                        >
                          Next
                          <ChevronRightIcon className="h-4 w-4 ml-2" />
                        </button>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="p-6 text-center text-gray-500">
                    No questions available in this section.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AssessmentTest;
