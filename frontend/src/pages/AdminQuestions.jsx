import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { questionService } from '../utils/adminApi';
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  QuestionMarkCircleIcon,
  FunnelIcon,
} from '@heroicons/react/24/outline';

const AdminQuestions = () => {
  const [questions, setQuestions] = useState([]);
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [searchParams] = useSearchParams();
  const [filters, setFilters] = useState({
    section: searchParams.get('section') || '',
    type: '',
    difficulty: ''
  });
  const [formData, setFormData] = useState({
    questionText: '',
    questionType: 'MCQ',
    difficultyLevel: 'EASY',
    sectionId: '',
    points: 10,
    timeLimitMinutes: 5,
    mcqOptions: [
      { optionText: '', isCorrect: false },
      { optionText: '', isCorrect: false },
      { optionText: '', isCorrect: false },
      { optionText: '', isCorrect: false }
    ],
    testCases: [{ input: '', expectedOutput: '' }]
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [questionsData, sectionsData] = await Promise.all([
        questionService.getQuestions().catch(() => []),
        questionService.getSections().catch(() => [])
      ]);
      
      // Ensure data is always an array
      setQuestions(Array.isArray(questionsData) ? questionsData : []);
      setSections(Array.isArray(sectionsData) ? sectionsData : []);
    } catch (error) {
      console.error('Error fetching data:', error);
      toast.error('Failed to load data');
      // Set empty arrays as fallback
      setQuestions([]);
      setSections([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredQuestions = questions.filter(question => {
    return (
      (!filters.section || question.section?.sectionId === parseInt(filters.section)) &&
      (!filters.type || question.questionType === filters.type) &&
      (!filters.difficulty || question.difficultyLevel === filters.difficulty)
    );
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.questionText.trim() || !formData.sectionId) {
      toast.error('Question text and section are required');
      return;
    }

    if (formData.questionType === 'MCQ') {
      const correctOptions = formData.mcqOptions.filter(opt => opt.isCorrect);
      if (correctOptions.length === 0) {
        toast.error('At least one MCQ option must be marked as correct');
        return;
      }
      const filledOptions = formData.mcqOptions.filter(opt => opt.optionText.trim());
      if (filledOptions.length < 2) {
        toast.error('At least 2 MCQ options are required');
        return;
      }
    }

    if (formData.questionType === 'CODING') {
      const validTestCases = formData.testCases.filter(tc => tc.input.trim() && tc.expectedOutput.trim());
      if (validTestCases.length === 0) {
        toast.error('At least one test case is required for coding questions');
        return;
      }
    }

    try {
      const questionData = {
        questionText: formData.questionText,
        questionType: formData.questionType,
        difficultyLevel: formData.difficultyLevel,
        points: formData.points || 10, // Default points
        timeLimitMinutes: formData.timeLimitMinutes || 5, // Default time limit
        mcqOptions: formData.questionType === 'MCQ' ? formData.mcqOptions.filter(opt => opt.optionText.trim()) : [],
        testCases: formData.questionType === 'CODING' ? formData.testCases.filter(tc => tc.input.trim() && tc.expectedOutput.trim()) : []
      };

      if (editingQuestion) {
        await questionService.updateQuestion(editingQuestion.questionId, questionData);
        toast.success('Question updated successfully');
      } else {
        const response = await questionService.createQuestion(formData.sectionId, questionData);
        toast.success('Question created successfully');
      }

      resetForm();
      fetchData();
    } catch (error) {
      console.error('Error saving question:', error);
      toast.error(editingQuestion ? 'Failed to update question' : 'Failed to create question');
    }
  };

  const handleEdit = async (question) => {
    setEditingQuestion(question);
    setFormData({
      questionText: question.questionText,
      questionType: question.questionType,
      difficultyLevel: question.difficultyLevel,
      sectionId: question.section?.sectionId || '',
      mcqOptions: [
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false }
      ],
      testCases: [{ input: '', expectedOutput: '' }]
    });

    // Load existing options/test cases
    try {
      if (question.questionType === 'MCQ') {
        const options = await questionService.getMcqOptions(question.questionId);
        if (options.length > 0) {
          const formattedOptions = [...options];
          while (formattedOptions.length < 4) {
            formattedOptions.push({ optionText: '', isCorrect: false });
          }
          setFormData(prev => ({ ...prev, mcqOptions: formattedOptions }));
        }
      } else if (question.questionType === 'CODING') {
        const testCases = await questionService.getTestCases(question.questionId);
        if (testCases.length > 0) {
          setFormData(prev => ({ ...prev, testCases }));
        }
      }
    } catch (error) {
      console.error('Error loading question details:', error);
    }

    setShowCreateModal(true);
  };

  const handleDelete = async (questionId) => {
    if (!window.confirm('Are you sure you want to delete this question? This action cannot be undone.')) {
      return;
    }

    try {
      await questionService.deleteQuestion(questionId);
      toast.success('Question deleted successfully');
      fetchData();
    } catch (error) {
      console.error('Error deleting question:', error);
      toast.error('Failed to delete question');
    }
  };

  const resetForm = () => {
    setFormData({
      questionText: '',
      questionType: 'MCQ',
      difficultyLevel: 'EASY',
      sectionId: '',
      points: 10,
      timeLimitMinutes: 5,
      mcqOptions: [
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false }
      ],
      testCases: [{ input: '', expectedOutput: '' }]
    });
    setEditingQuestion(null);
    setShowCreateModal(false);
  };

  const addTestCase = () => {
    setFormData(prev => ({
      ...prev,
      testCases: [...prev.testCases, { input: '', expectedOutput: '' }]
    }));
  };

  const removeTestCase = (index) => {
    setFormData(prev => ({
      ...prev,
      testCases: prev.testCases.filter((_, i) => i !== index)
    }));
  };

  const updateMcqOption = (index, field, value) => {
    setFormData(prev => ({
      ...prev,
      mcqOptions: prev.mcqOptions.map((opt, i) => 
        i === index ? { ...opt, [field]: value } : opt
      )
    }));
  };

  const updateTestCase = (index, field, value) => {
    setFormData(prev => ({
      ...prev,
      testCases: prev.testCases.map((tc, i) => 
        i === index ? { ...tc, [field]: value } : tc
      )
    }));
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Questions</h1>
          <p className="text-gray-600">Manage MCQ and coding questions</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
        >
          <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
          New Question
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow">
        <div className="flex items-center space-x-4">
          <FunnelIcon className="h-5 w-5 text-gray-400" />
          <select
            value={filters.section}
            onChange={(e) => setFilters({ ...filters, section: e.target.value })}
            className="border-gray-300 rounded-md text-sm"
          >
            <option value="">All Sections</option>
            {sections.map(section => (
              <option key={section.sectionId} value={section.sectionId}>
                {section.name}
              </option>
            ))}
          </select>
          <select
            value={filters.type}
            onChange={(e) => setFilters({ ...filters, type: e.target.value })}
            className="border-gray-300 rounded-md text-sm"
          >
            <option value="">All Types</option>
            <option value="MCQ">MCQ</option>
            <option value="CODING">Coding</option>
          </select>
          <select
            value={filters.difficulty}
            onChange={(e) => setFilters({ ...filters, difficulty: e.target.value })}
            className="border-gray-300 rounded-md text-sm"
          >
            <option value="">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
      </div>

      {/* Questions List */}
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        {filteredQuestions.length === 0 ? (
          <div className="text-center py-12">
            <QuestionMarkCircleIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No questions</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by creating a new question.</p>
            <div className="mt-6">
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
              >
                <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                New Question
              </button>
            </div>
          </div>
        ) : (
          <ul className="divide-y divide-gray-200">
            {filteredQuestions.map((question) => (
              <li key={question.questionId}>
                <div className="px-4 py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3">
                        <QuestionMarkCircleIcon className="h-6 w-6 text-green-500" />
                        <div className="flex-1">
                          <p className="text-sm font-medium text-gray-900">
                            {question.questionText.substring(0, 100)}
                            {question.questionText.length > 100 && '...'}
                          </p>
                          <div className="flex items-center space-x-4 mt-2">
                            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                              question.questionType === 'MCQ' 
                                ? 'bg-blue-100 text-blue-800' 
                                : 'bg-green-100 text-green-800'
                            }`}>
                              {question.questionType}
                            </span>
                            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                              question.difficultyLevel === 'EASY' ? 'bg-green-100 text-green-800' :
                              question.difficultyLevel === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                              'bg-red-100 text-red-800'
                            }`}>
                              {question.difficultyLevel}
                            </span>
                            {question.section && (
                              <span className="text-xs text-gray-500">
                                Section: {question.section.name}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleEdit(question)}
                        className="text-gray-400 hover:text-gray-600"
                      >
                        <PencilIcon className="h-5 w-5" />
                      </button>
                      <button
                        onClick={() => handleDelete(question.questionId)}
                        className="text-red-400 hover:text-red-600"
                      >
                        <TrashIcon className="h-5 w-5" />
                      </button>
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Create/Edit Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-10 mx-auto p-5 border w-full max-w-2xl shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingQuestion ? 'Edit Question' : 'Create New Question'}
              </h3>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Section *
                    </label>
                    <select
                      value={formData.sectionId}
                      onChange={(e) => setFormData({ ...formData, sectionId: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                      required
                    >
                      <option value="">Select Section</option>
                      {sections.map(section => (
                        <option key={section.sectionId} value={section.sectionId}>
                          {section.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Question Type *
                    </label>
                    <select
                      value={formData.questionType}
                      onChange={(e) => setFormData({ ...formData, questionType: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                    >
                      <option value="MCQ">Multiple Choice</option>
                      <option value="CODING">Coding</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Difficulty Level *
                  </label>
                  <select
                    value={formData.difficultyLevel}
                    onChange={(e) => setFormData({ ...formData, difficultyLevel: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Question Text *
                  </label>
                  <textarea
                    rows={4}
                    value={formData.questionText}
                    onChange={(e) => setFormData({ ...formData, questionText: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                    placeholder="Enter the question text"
                    required
                  />
                </div>

                {/* MCQ Options */}
                {formData.questionType === 'MCQ' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Answer Options
                    </label>
                    {formData.mcqOptions.map((option, index) => (
                      <div key={index} className="flex items-center space-x-2 mb-2">
                        <input
                          type="checkbox"
                          checked={option.isCorrect}
                          onChange={(e) => updateMcqOption(index, 'isCorrect', e.target.checked)}
                          className="rounded border-gray-300 text-green-600 focus:ring-green-500"
                        />
                        <input
                          type="text"
                          value={option.optionText}
                          onChange={(e) => updateMcqOption(index, 'optionText', e.target.value)}
                          className="flex-1 border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500"
                          placeholder={`Option ${index + 1}`}
                        />
                      </div>
                    ))}
                    <p className="text-xs text-gray-500">Check the box to mark correct answers</p>
                  </div>
                )}

                {/* Test Cases */}
                {formData.questionType === 'CODING' && (
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <label className="block text-sm font-medium text-gray-700">
                        Test Cases
                      </label>
                      <button
                        type="button"
                        onClick={addTestCase}
                        className="text-sm text-green-600 hover:text-green-800"
                      >
                        + Add Test Case
                      </button>
                    </div>
                    {formData.testCases.map((testCase, index) => (
                      <div key={index} className="border rounded-md p-3 mb-3">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-sm font-medium">Test Case {index + 1}</span>
                          {formData.testCases.length > 1 && (
                            <button
                              type="button"
                              onClick={() => removeTestCase(index)}
                              className="text-red-600 hover:text-red-800 text-sm"
                            >
                              Remove
                            </button>
                          )}
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                          <div>
                            <label className="block text-xs font-medium text-gray-700">Input</label>
                            <textarea
                              rows={2}
                              value={testCase.input}
                              onChange={(e) => updateTestCase(index, 'input', e.target.value)}
                              className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500 text-sm"
                              placeholder="Input data"
                            />
                          </div>
                          <div>
                            <label className="block text-xs font-medium text-gray-700">Expected Output</label>
                            <textarea
                              rows={2}
                              value={testCase.expectedOutput}
                              onChange={(e) => updateTestCase(index, 'expectedOutput', e.target.value)}
                              className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-green-500 focus:border-green-500 text-sm"
                              placeholder="Expected output"
                            />
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={resetForm}
                    className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700"
                  >
                    {editingQuestion ? 'Update' : 'Create'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminQuestions;
