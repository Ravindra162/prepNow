import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { questionService, assessmentService } from '../utils/adminApi';
import {
  DocumentTextIcon,
  QuestionMarkCircleIcon,
  ClipboardDocumentListIcon,
  BuildingOfficeIcon,
  PlusIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    sections: 0,
    questions: 0,
    assessments: 0,
    companies: 0,
  });
  const [loading, setLoading] = useState(true);
  const [recentSections, setRecentSections] = useState([]);
  const [recentQuestions, setRecentQuestions] = useState([]);

  useEffect(() => {
    let isMounted = true;
    const abortController = new AbortController();
    
    const loadData = async () => {
      if (isMounted && !abortController.signal.aborted) {
        await fetchDashboardData(abortController.signal);
      }
    };
    
    loadData();
    
    return () => {
      isMounted = false;
      abortController.abort();
    };
  }, []);

  const fetchDashboardData = async (signal) => {
    try {
      setLoading(true);
      
      // Check if request was aborted
      if (signal?.aborted) {
        return;
      }
      
      // Fetch statistics with proper error handling
      const [sectionsResponse, questionsResponse, companiesResponse] = await Promise.all([
        questionService.getSections().catch((error) => {
          if (error.name === 'AbortError') return [];
          console.error('Error fetching sections:', error);
          return [];
        }),
        questionService.getQuestions().catch((error) => {
          if (error.name === 'AbortError') return [];
          console.error('Error fetching questions:', error);
          return [];
        }),
        assessmentService.getCompanies().catch((error) => {
          if (error.name === 'AbortError') return [];
          console.error('Error fetching companies:', error);
          return [];
        }),
      ]);

      // Check if request was aborted after initial calls
      if (signal?.aborted) {
        return;
      }

      // Ensure all responses are arrays
      const sections = Array.isArray(sectionsResponse) ? sectionsResponse : [];
      const questions = Array.isArray(questionsResponse) ? questionsResponse : [];
      const companies = Array.isArray(companiesResponse) ? companiesResponse : [];

      // Get assessments count from all companies (limit to prevent infinite calls)
      let totalAssessments = 0;
      if (companies.length > 0 && companies.length < 100) { // Safety limit
        try {
          const assessmentPromises = companies.map(company => 
            assessmentService.getAssessmentsByCompany(company.companyId).catch((error) => {
              if (error.name === 'AbortError') return [];
              console.error(`Error fetching assessments for company ${company.companyId}:`, error);
              return [];
            })
          );
          
          const assessmentArrays = await Promise.all(assessmentPromises);
          
          // Check if request was aborted after assessment calls
          if (signal?.aborted) {
            return;
          }
          
          totalAssessments = assessmentArrays.reduce((total, arr) => {
            return total + (Array.isArray(arr) ? arr.length : 0);
          }, 0);
        } catch (error) {
          console.error('Error fetching assessments:', error);
          totalAssessments = 0;
        }
      }

      // Final abort check before setting state
      if (signal?.aborted) {
        return;
      }

      setStats({
        sections: sections.length,
        questions: questions.length,
        assessments: totalAssessments,
        companies: companies.length,
      });

      // Set recent items (last 5)
      setRecentSections(sections.slice(-5).reverse());
      setRecentQuestions(questions.slice(-5).reverse());

    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('Dashboard data fetch was aborted');
        return;
      }
      console.error('Error fetching dashboard data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      if (!signal?.aborted) {
        setLoading(false);
      }
    }
  };

  const statCards = [
    {
      name: 'Total Sections',
      value: stats.sections,
      icon: DocumentTextIcon,
      color: 'bg-blue-500',
      href: '/admin/sections',
    },
    {
      name: 'Total Questions',
      value: stats.questions,
      icon: QuestionMarkCircleIcon,
      color: 'bg-green-500',
      href: '/admin/questions',
    },
    {
      name: 'Total Assessments',
      value: stats.assessments,
      icon: ClipboardDocumentListIcon,
      color: 'bg-purple-500',
      href: '/admin/assessments',
    },
    {
      name: 'Total Companies',
      value: stats.companies,
      icon: BuildingOfficeIcon,
      color: 'bg-orange-500',
      href: '/admin/companies',
    },
  ];

  const quickActions = [
    {
      name: 'Create Section',
      description: 'Add a new question section',
      href: '/admin/sections',
      icon: DocumentTextIcon,
      color: 'bg-blue-50 text-blue-600 hover:bg-blue-100',
    },
    {
      name: 'Add Question',
      description: 'Create a new question',
      href: '/admin/questions',
      icon: QuestionMarkCircleIcon,
      color: 'bg-green-50 text-green-600 hover:bg-green-100',
    },
    {
      name: 'Create Assessment',
      description: 'Build a new assessment',
      href: '/admin/assessments',
      icon: ClipboardDocumentListIcon,
      color: 'bg-purple-50 text-purple-600 hover:bg-purple-100',
    },
    {
      name: 'Add Company',
      description: 'Register a new company',
      href: '/admin/companies',
      icon: BuildingOfficeIcon,
      color: 'bg-orange-50 text-orange-600 hover:bg-orange-100',
    },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center">
          <ChartBarIcon className="h-8 w-8 text-blue-600 mr-3" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-gray-600">Manage questions, sections, and assessments</p>
          </div>
        </div>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <Link
              key={card.name}
              to={card.href}
              className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow"
            >
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className={`${card.color} rounded-md p-3`}>
                      <Icon className="h-6 w-6 text-white" />
                    </div>
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        {card.name}
                      </dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {card.value}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Quick Actions</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {quickActions.map((action) => {
              const Icon = action.icon;
              return (
                <Link
                  key={action.name}
                  to={action.href}
                  className={`${action.color} rounded-lg p-4 block hover:shadow-md transition-all`}
                >
                  <div className="flex items-center">
                    <Icon className="h-6 w-6 mr-3" />
                    <div>
                      <h3 className="text-sm font-medium">{action.name}</h3>
                      <p className="text-xs opacity-75 mt-1">{action.description}</p>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        </div>
      </div>

      {/* Recent Items */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Sections */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-medium text-gray-900">Recent Sections</h2>
              <Link
                to="/admin/sections"
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                View all
              </Link>
            </div>
          </div>
          <div className="p-6">
            {recentSections.length > 0 ? (
              <div className="space-y-3">
                {recentSections.map((section) => (
                  <div
                    key={section.sectionId}
                    className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                  >
                    <div>
                      <h3 className="text-sm font-medium text-gray-900">
                        {section.name}
                      </h3>
                      <p className="text-xs text-gray-500">{section.description}</p>
                    </div>
                    <DocumentTextIcon className="h-5 w-5 text-gray-400" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-4">
                <DocumentTextIcon className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900">No sections</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by creating a section.</p>
                <div className="mt-6">
                  <Link
                    to="/admin/sections"
                    className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                  >
                    <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                    New Section
                  </Link>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Recent Questions */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-medium text-gray-900">Recent Questions</h2>
              <Link
                to="/admin/questions"
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                View all
              </Link>
            </div>
          </div>
          <div className="p-6">
            {recentQuestions.length > 0 ? (
              <div className="space-y-3">
                {recentQuestions.map((question) => (
                  <div
                    key={question.questionId}
                    className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                  >
                    <div>
                      <h3 className="text-sm font-medium text-gray-900">
                        {question.questionText.substring(0, 50)}...
                      </h3>
                      <div className="flex items-center space-x-2 mt-1">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                          question.questionType === 'MCQ' 
                            ? 'bg-blue-100 text-blue-800' 
                            : 'bg-green-100 text-green-800'
                        }`}>
                          {question.questionType}
                        </span>
                        <span className="text-xs text-gray-500">
                          {question.difficultyLevel}
                        </span>
                      </div>
                    </div>
                    <QuestionMarkCircleIcon className="h-5 w-5 text-gray-400" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-4">
                <QuestionMarkCircleIcon className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900">No questions</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by creating a question.</p>
                <div className="mt-6">
                  <Link
                    to="/admin/questions"
                    className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                  >
                    <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
                    New Question
                  </Link>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
