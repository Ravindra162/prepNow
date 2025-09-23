import axios from 'axios';

// API endpoints for different services
const QUESTION_SERVICE_URL = 'http://localhost:8082';
const ASSESSMENT_SERVICE_URL = 'http://localhost:8081';

// Create axios instances for different services
const questionApi = axios.create({
  baseURL: QUESTION_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const assessmentApi = axios.create({
  baseURL: ASSESSMENT_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add response interceptors for error handling
const handleApiError = (error) => {
  console.error('API Error:', error);
  
  if (error.response?.status === 401) {
    // Handle unauthorized access
    localStorage.removeItem('token');
    window.location.href = '/login';
  }
  
  // Handle JSON parsing errors
  if (error.message && error.message.includes('JSON.parse')) {
    console.error('JSON parsing error - response might be malformed');
    console.error('Raw response:', error.response?.data);
  }
  
  if (error.response) {
    console.error('API Error:', error.response.data);
    throw error.response.data || 'Something went wrong';
  } else if (error.request) {
    console.error('API Error:', error.request);
    throw 'No response from server. Please check your connection.';
  } else {
    console.error('API Error:', error.message);
    throw error.message;
  }
};

questionApi.interceptors.response.use(
  (response) => response,
  handleApiError
);

assessmentApi.interceptors.response.use(
  (response) => response,
  handleApiError
);

// Question Service APIs
export const questionService = {
  // Questions
  async getQuestions() {
    try {
      const response = await questionApi.get('/questions');
      console.log('Questions API response:', response);
      
      let data = response.data;
      
      // Handle case where data is a JSON string instead of object
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch (parseError) {
          console.error('Failed to parse JSON string:', parseError);
          return [];
        }
      }
      
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Error fetching questions:', error);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
      }
      throw error;
    }
  },

  async getSections() {
    try {
      const response = await questionApi.get('/sections');
      console.log('Sections API response:', response);
      
      let data = response.data;
      
      // Handle case where data is a JSON string instead of object
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch (parseError) {
          console.error('Failed to parse JSON string:', parseError);
          return [];
        }
      }
      
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error('Error fetching sections:', error);
      if (error.response) {
        console.error('Response data:', error.response.data);
        console.error('Response status:', error.response.status);
      }
      throw error;
    }
  },

  createSection: async (sectionData) => {
    const response = await questionApi.post('/sections', sectionData);
    return response.data;
  },

  getSectionById: async (sectionId) => {
    const response = await questionApi.get(`/sections/${sectionId}`);
    return response.data;
  },

  updateSection: async (sectionId, sectionData) => {
    const response = await questionApi.put(`/sections/${sectionId}`, sectionData);
    return response.data;
  },

  deleteSection: async (sectionId) => {
    const response = await questionApi.delete(`/sections/${sectionId}`);
    return response.data;
  },

  // Question APIs
  createQuestion: async (sectionId, questionData) => {
    const response = await questionApi.post(`/questions/sections/${sectionId}`, questionData);
    return response.data;
  },

  getQuestions: async () => {
    const response = await questionApi.get('/questions');
    return response.data;
  },

  getQuestionsBySection: async (sectionId) => {
    try {
      const response = await questionApi.get(`/questions/sections/${sectionId}`);
      let questions = response.data;
      
      // Fetch MCQ options and test cases for each question
      for (let question of questions) {
        if (question.type === 'MCQ') {
          try {
            const mcqOptions = await questionApi.get(`/questions/${question.questionId}/mcq-options`);
            question.mcqOptions = mcqOptions.data;
          } catch (error) {
            console.error(`Error fetching MCQ options for question ${question.questionId}:`, error);
            question.mcqOptions = [];
          }
        } else if (question.type === 'CODING') {
          try {
            const testCases = await questionApi.get(`/questions/${question.questionId}/test-cases`);
            question.testCases = testCases.data;
          } catch (error) {
            console.error(`Error fetching test cases for question ${question.questionId}:`, error);
            question.testCases = [];
          }
        }
      }
      
      return questions;
    } catch (error) {
      console.error('Error fetching questions by section:', error);
      throw error;
    }
  },

  getQuestionById: async (questionId) => {
    try {
      const response = await questionApi.get(`/questions/${questionId}`);
      let question = response.data;
      
      // Fetch MCQ options if it's an MCQ question
      if (question.type === 'MCQ') {
        try {
          const mcqOptions = await questionApi.get(`/questions/${questionId}/mcq-options`);
          question.mcqOptions = mcqOptions.data;
        } catch (error) {
          console.error(`Error fetching MCQ options for question ${questionId}:`, error);
          question.mcqOptions = [];
        }
      }
      
      // Fetch test cases if it's a coding question
      if (question.type === 'CODING') {
        try {
          const testCases = await questionApi.get(`/questions/${questionId}/test-cases`);
          question.testCases = testCases.data;
        } catch (error) {
          console.error(`Error fetching test cases for question ${questionId}:`, error);
          question.testCases = [];
        }
      }
      
      return question;
    } catch (error) {
      console.error('Error fetching question by ID:', error);
      throw error;
    }
  },

  updateQuestion: async (questionId, questionData) => {
    const response = await questionApi.put(`/questions/${questionId}`, questionData);
    return response.data;
  },

  deleteQuestion: async (questionId) => {
    const response = await questionApi.delete(`/questions/${questionId}`);
    return response.data;
  },

  // MCQ Options APIs
  addMcqOption: async (questionId, option) => {
    const response = await questionApi.post(`/questions/${questionId}/mcq-options`, option);
    return response.data;
  },

  addMcqOptions: async (questionId, options) => {
    const response = await questionApi.post(`/questions/${questionId}/mcq-options/batch`, options);
    return response.data;
  },

  addMcqOptionsFromArray: async (questionId, options) => {
    const response = await questionApi.post(`/questions/${questionId}/mcq-options/array`, options);
    return response.data;
  },

  getMcqOptions: async (questionId) => {
    const response = await questionApi.get(`/questions/${questionId}/mcq-options`);
    return response.data;
  },

  deleteMcqOption: async (optionId) => {
    const response = await questionApi.delete(`/mcq-options/${optionId}`);
    return response.data;
  },

  // Test Cases APIs
  addTestCase: async (questionId, testCase) => {
    const response = await questionApi.post(`/questions/${questionId}/test-cases`, testCase);
    return response.data;
  },

  addTestCases: async (questionId, testCases) => {
    const response = await questionApi.post(`/questions/${questionId}/test-cases/batch`, testCases);
    return response.data;
  },

  getTestCases: async (questionId) => {
    const response = await questionApi.get(`/questions/${questionId}/test-cases`);
    return response.data;
  },

  getSampleTestCases: async (questionId) => {
    const response = await questionApi.get(`/questions/${questionId}/test-cases/sample`);
    return response.data;
  },

  deleteTestCase: async (testCaseId) => {
    const response = await questionApi.delete(`/test-cases/${testCaseId}`);
    return response.data;
  },
};

// Assessment Service APIs
export const assessmentService = {
  // Company APIs
  createCompany: async (companyData) => {
    const response = await assessmentApi.post('/companies', companyData);
    return response.data;
  },

  getCompanies: async () => {
    const response = await assessmentApi.get('/companies');
    return response.data;
  },

  getCompanyById: async (companyId) => {
    const response = await assessmentApi.get(`/companies/${companyId}`);
    return response.data;
  },

  updateCompany: async (companyId, companyData) => {
    const response = await assessmentApi.put(`/companies/${companyId}`, companyData);
    return response.data;
  },

  deleteCompany: async (companyId) => {
    const response = await assessmentApi.delete(`/companies/${companyId}`);
    return response.data;
  },

  // Assessment APIs
  createAssessment: async (companyId, assessmentData) => {
    const response = await assessmentApi.post(`/companies/${companyId}/assessments`, assessmentData);
    return response.data;
  },

  getAssessmentsByCompany: async (companyId) => {
    const response = await assessmentApi.get(`/companies/${companyId}/assessments`);
    return response.data;
  },

  getAssessmentById: async (assessmentId) => {
    const response = await assessmentApi.get(`/assessments/${assessmentId}`);
    return response.data;
  },

  updateAssessment: async (assessmentId, assessmentData) => {
    const response = await assessmentApi.put(`/assessments/${assessmentId}`, assessmentData);
    return response.data;
  },

  deleteAssessment: async (assessmentId) => {
    const response = await assessmentApi.delete(`/assessments/${assessmentId}`);
    return response.data;
  },

  // Assessment Candidate APIs
  addCandidate: async (assessmentId, candidateData) => {
    const response = await assessmentApi.post(`/assessments/${assessmentId}/candidates`, candidateData);
    return response.data;
  },

  getCandidates: async (assessmentId) => {
    const response = await assessmentApi.get(`/assessments/${assessmentId}/candidates`);
    return response.data;
  },

  updateCandidate: async (assessmentId, candidateId, candidateData) => {
    const response = await assessmentApi.put(`/assessments/${assessmentId}/candidates/${candidateId}`, candidateData);
    return response.data;
  },

  deleteCandidate: async (assessmentId, candidateId) => {
    const response = await assessmentApi.delete(`/assessments/${assessmentId}/candidates/${candidateId}`);
    return response.data;
  },
};

export default { questionService, assessmentService };
