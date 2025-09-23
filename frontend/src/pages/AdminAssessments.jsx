import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { questionService, assessmentService } from '../utils/adminApi';
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  ClipboardDocumentListIcon,
  CheckIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';

const AdminAssessments = () => {
  const [assessments, setAssessments] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [sections, setSections] = useState([]);
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingAssessment, setEditingAssessment] = useState(null);
  const [selectedSections, setSelectedSections] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    companyId: '',
    createdBy: 'admin',
    scheduledAt: '',
    durationMinutes: 60,
    structure: {}
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [companiesData, sectionsData, questionsData] = await Promise.all([
        assessmentService.getCompanies(),
        questionService.getSections(),
        questionService.getQuestions()
      ]);
      
      setCompanies(companiesData);
      setSections(sectionsData);
      setQuestions(questionsData);

      // Fetch assessments from all companies
      const allAssessments = [];
      for (const company of companiesData) {
        try {
          const companyAssessments = await assessmentService.getAssessmentsByCompany(company.companyId);
          allAssessments.push(...companyAssessments.map(assessment => ({
            ...assessment,
            companyName: company.name
          })));
        } catch (error) {
          console.error(`Error fetching assessments for company ${company.companyId}:`, error);
        }
      }
      setAssessments(allAssessments);
    } catch (error) {
      console.error('Error fetching data:', error);
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.name.trim() || !formData.companyId) {
      toast.error('Assessment name and company are required');
      return;
    }

    if (selectedSections.length === 0) {
      toast.error('Please select at least one section for the assessment');
      return;
    }

    try {
      // Build assessment structure with selected sections and all their questions
      const structure = {
        sections: selectedSections.map(section => {
          const sectionQuestions = questions.filter(q => q.section?.sectionId === section.sectionId);
          return {
            sectionId: section.sectionId,
            sectionName: section.name,
            sectionDescription: section.description,
            questions: sectionQuestions.map(q => ({
              questionId: q.questionId,
              questionText: q.questionText,
              questionType: q.type,
              difficultyLevel: q.difficultyLevel,
              points: q.points,
              timeLimitMinutes: q.timeLimitMinutes
            }))
          };
        })
      };

      const assessmentData = {
        ...formData,
        structure: structure
      };

      if (editingAssessment) {
        await assessmentService.updateAssessment(editingAssessment.assessmentId, assessmentData);
        toast.success('Assessment updated successfully');
      } else {
        await assessmentService.createAssessment(formData.companyId, assessmentData);
        toast.success('Assessment created successfully');
      }
      
      resetForm();
      fetchData();
    } catch (error) {
      console.error('Error saving assessment:', error);
      toast.error(editingAssessment ? 'Failed to update assessment' : 'Failed to create assessment');
    }
  };

  const handleEdit = (assessment) => {
    setEditingAssessment(assessment);
    setFormData({
      name: assessment.name,
      description: assessment.description || '',
      companyId: assessment.companyId,
      createdBy: assessment.createdBy,
      scheduledAt: assessment.scheduledAt ? new Date(assessment.scheduledAt).toISOString().slice(0, 16) : '',
      durationMinutes: assessment.durationMinutes,
      structure: assessment.structure
    });

    // Parse and set selected sections
    try {
      const structure = typeof assessment.structure === 'string' 
        ? JSON.parse(assessment.structure) 
        : assessment.structure;
      
      const sectionsFromStructure = [];
      if (structure.sections) {
        structure.sections.forEach(structureSection => {
          const fullSection = sections.find(section => section.sectionId === structureSection.sectionId);
          if (fullSection) {
            sectionsFromStructure.push(fullSection);
          }
        });
      }
      setSelectedSections(sectionsFromStructure);
    } catch (error) {
      console.error('Error parsing assessment structure:', error);
      setSelectedSections([]);
    }

    setShowCreateModal(true);
  };

  const handleDelete = async (assessmentId) => {
    if (!window.confirm('Are you sure you want to delete this assessment? This action cannot be undone.')) {
      return;
    }

    try {
      await assessmentService.deleteAssessment(assessmentId);
      toast.success('Assessment deleted successfully');
      fetchData();
    } catch (error) {
      console.error('Error deleting assessment:', error);
      toast.error('Failed to delete assessment');
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      companyId: '',
      createdBy: 'admin',
      scheduledAt: '',
      durationMinutes: 60,
      structure: {}
    });
    setSelectedSections([]);
    setEditingAssessment(null);
    setShowCreateModal(false);
  };

  const toggleSectionSelection = (section) => {
    setSelectedSections(prev => {
      const isSelected = prev.some(s => s.sectionId === section.sectionId);
      if (isSelected) {
        return prev.filter(s => s.sectionId !== section.sectionId);
      } else {
        return [...prev, section];
      }
    });
  };

  const getQuestionsBySection = (sectionId) => {
    return questions.filter(q => q.section?.sectionId === sectionId);
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
          <h1 className="text-2xl font-bold text-gray-900">Assessments</h1>
          <p className="text-gray-600">Create and manage assessments by selecting sections</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
        >
          <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
          New Assessment
        </button>
      </div>

      {/* Assessments List */}
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        {assessments.length === 0 ? (
          <div className="text-center py-12">
            <ClipboardDocumentListIcon className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">No assessments</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by creating a new assessment.</p>
            <div className="mt-6">
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700"
              >
                <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                New Assessment
              </button>
            </div>
          </div>
        ) : (
          <ul className="divide-y divide-gray-200">
            {assessments.map((assessment) => (
              <li key={assessment.assessmentId}>
                <div className="px-4 py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <ClipboardDocumentListIcon className="h-8 w-8 text-purple-500 mr-4" />
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {assessment.name}
                        </div>
                        <div className="text-sm text-gray-500">
                          {assessment.description}
                        </div>
                        <div className="flex items-center space-x-4 mt-2">
                          <span className="text-xs text-gray-500">
                            Company: {assessment.companyName}
                          </span>
                          <span className="text-xs text-gray-500">
                            Duration: {assessment.durationMinutes} minutes
                          </span>
                          {assessment.scheduledAt && (
                            <span className="text-xs text-gray-500">
                              Scheduled: {new Date(assessment.scheduledAt).toLocaleString()}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleEdit(assessment)}
                        className="text-gray-400 hover:text-gray-600"
                      >
                        <PencilIcon className="h-5 w-5" />
                      </button>
                      <button
                        onClick={() => handleDelete(assessment.assessmentId)}
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
          <div className="relative top-5 mx-auto p-5 border w-full max-w-4xl shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingAssessment ? 'Edit Assessment' : 'Create New Assessment'}
              </h3>
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Basic Info */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Assessment Name *
                    </label>
                    <input
                      type="text"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                      placeholder="Enter assessment name"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Company *
                    </label>
                    <select
                      value={formData.companyId}
                      onChange={(e) => setFormData({ ...formData, companyId: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                      required
                    >
                      <option value="">Select Company</option>
                      {companies.map(company => (
                        <option key={company.companyId} value={company.companyId}>
                          {company.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Description
                  </label>
                  <textarea
                    rows={3}
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                    placeholder="Enter assessment description"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Duration (minutes)
                    </label>
                    <input
                      type="number"
                      value={formData.durationMinutes}
                      onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                      min="1"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Scheduled At
                    </label>
                    <input
                      type="datetime-local"
                      value={formData.scheduledAt}
                      onChange={(e) => setFormData({ ...formData, scheduledAt: e.target.value })}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                    />
                  </div>
                </div>

                {/* Section Selection */}
                <div>
                  <h4 className="text-md font-medium text-gray-900 mb-3">
                    Select Sections ({selectedSections.length} selected)
                  </h4>
                  <div className="max-h-96 overflow-y-auto border border-gray-200 rounded-md">
                    {sections.map(section => {
                      const sectionQuestions = getQuestionsBySection(section.sectionId);
                      const isSelected = selectedSections.some(s => s.sectionId === section.sectionId);
                      
                      return (
                        <div
                          key={section.sectionId}
                          className={`border-b border-gray-200 last:border-b-0 cursor-pointer transition-colors ${
                            isSelected ? 'bg-purple-50 border-l-4 border-l-purple-500' : 'hover:bg-gray-50'
                          }`}
                          onClick={() => toggleSectionSelection(section)}
                        >
                          <div className="px-4 py-4">
                            <div className="flex items-center space-x-3">
                              <div className={`flex-shrink-0 w-5 h-5 rounded border-2 flex items-center justify-center ${
                                isSelected ? 'bg-purple-600 border-purple-600' : 'border-gray-300'
                              }`}>
                                {isSelected && <CheckIcon className="w-3 h-3 text-white" />}
                              </div>
                              <div className="flex-1">
                                <h5 className="font-medium text-gray-900">{section.name}</h5>
                                {section.description && (
                                  <p className="text-sm text-gray-500 mt-1">{section.description}</p>
                                )}
                                <div className="flex items-center space-x-4 mt-2">
                                  <span className="text-xs text-gray-500">
                                    {sectionQuestions.length} questions
                                  </span>
                                  <div className="flex space-x-1">
                                    {['EASY', 'MEDIUM', 'HARD'].map(level => {
                                      const count = sectionQuestions.filter(q => q.difficultyLevel === level).length;
                                      if (count === 0) return null;
                                      return (
                                        <span key={level} className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                                          level === 'EASY' ? 'bg-green-100 text-green-800' :
                                          level === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                                          'bg-red-100 text-red-800'
                                        }`}>
                                          {count} {level}
                                        </span>
                                      );
                                    })}
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                  
                  {/* Selected Sections Summary */}
                  {selectedSections.length > 0 && (
                    <div className="mt-4 p-4 bg-purple-50 rounded-md">
                      <h5 className="font-medium text-purple-900 mb-2">Selected Sections Summary:</h5>
                      <div className="space-y-2">
                        {selectedSections.map(section => {
                          const sectionQuestions = getQuestionsBySection(section.sectionId);
                          return (
                            <div key={section.sectionId} className="flex justify-between items-center text-sm">
                              <span className="text-purple-800">{section.name}</span>
                              <span className="text-purple-600">{sectionQuestions.length} questions</span>
                            </div>
                          );
                        })}
                      </div>
                      <div className="mt-2 pt-2 border-t border-purple-200">
                        <div className="flex justify-between items-center text-sm font-medium">
                          <span className="text-purple-900">Total Questions:</span>
                          <span className="text-purple-700">
                            {selectedSections.reduce((total, section) => {
                              return total + getQuestionsBySection(section.sectionId).length;
                            }, 0)}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>

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
                    className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-600 hover:bg-purple-700"
                  >
                    {editingAssessment ? 'Update' : 'Create'}
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

export default AdminAssessments;
