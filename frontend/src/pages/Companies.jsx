import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';
import { assessmentService } from '../utils/adminApi';
import {
  BuildingOfficeIcon,
  DocumentTextIcon,
  PlayIcon,
} from '@heroicons/react/24/outline';

const Companies = () => {
  const { currentUser, logout } = useAuth();
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [assessments, setAssessments] = useState({});
  const [logoutLoading, setLogoutLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCompanies();
  }, []);

  const fetchCompanies = async () => {
    try {
      setLoading(true);
      const data = await assessmentService.getCompanies();
      setCompanies(data);
      
      // Fetch assessments for all companies
      const assessmentsData = {};
      for (const company of data) {
        try {
          const assessments = await assessmentService.getAssessmentsByCompany(company.companyId);
          console.log(`Assessments for company ${company.name}:`, assessments);
          assessmentsData[company.companyId] = assessments;
        } catch (error) {
          console.error(`Error fetching assessments for company ${company.companyId}:`, error);
          assessmentsData[company.companyId] = [];
        }
      }
      setAssessments(assessmentsData);
    } catch (error) {
      console.error('Error fetching companies:', error);
      toast.error('Failed to load companies');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      setLogoutLoading(true);
      await logout();
      toast.success('Successfully logged out');
      navigate('/login');
    } catch (error) {
      toast.error('Failed to log out');
    } finally {
      setLogoutLoading(false);
    }
  };

  const handleAttemptAssessment = (assessmentId) => {
    navigate(`/assessment/${assessmentId}/instructions`);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <div className="flex-shrink-0 flex items-center">
                <span className="text-xl font-bold text-indigo-600">PrepNow</span>
              </div>
              <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
                <Link to="/dashboard" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Dashboard
                </Link>
                <Link to="/companies" className="border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Assessments
                </Link>
                <a href="#" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Practice
                </a>
                <a href="#" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Progress
                </a>
                <a href="#" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Resources
                </a>
                <Link to="/admin" className="border-transparent text-orange-500 hover:border-orange-300 hover:text-orange-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Admin Panel
                </Link>
              </div>
            </div>
            <div className="hidden sm:ml-6 sm:flex sm:items-center">
              <div className="ml-3 relative">
                <div>
                  <button type="button" className="bg-white rounded-full flex text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500" id="user-menu" aria-expanded="false" aria-haspopup="true">
                    <span className="sr-only">Open user menu</span>
                    <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-medium">
                      {currentUser?.name?.[0]?.toUpperCase() || 'U'}
                    </div>
                  </button>
                </div>
              </div>
              <button
                onClick={handleLogout}
                disabled={logoutLoading}
                className="ml-4 px-4 py-2 border border-transparent text-sm font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {logoutLoading ? 'Signing out...' : 'Sign out'}
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="py-10">
        {/* Header */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Companies & Assessments</h1>
          <p className="text-gray-600 mt-2">Explore available assessments by company</p>
        </div>

        {/* Companies List */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="space-y-4">
            {loading ? (
              <div className="text-center py-12 bg-white rounded-lg shadow">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                <p className="text-sm text-gray-500 mt-2">Loading companies and assessments...</p>
              </div>
            ) : companies.length === 0 ? (
              <div className="text-center py-12 bg-white rounded-lg shadow">
                <BuildingOfficeIcon className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900">No companies available</h3>
                <p className="mt-1 text-sm text-gray-500">Companies will be added soon.</p>
              </div>
            ) : (
              companies.map((company) => (
                <div key={company.companyId} className="bg-white shadow rounded-lg overflow-hidden">
                  {/* Company Header */}
                  <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                    <div className="flex items-center">
                      <div className="flex-shrink-0">
                        <BuildingOfficeIcon className="h-8 w-8 text-orange-500" />
                      </div>
                      <div className="ml-4">
                        <div className="text-lg font-medium text-gray-900">
                          {company.name}
                        </div>
                        {company.description && (
                          <div className="text-sm text-gray-500 mt-1">
                            {company.description}
                          </div>
                        )}
                        {company.domain && (
                          <div className="text-xs text-gray-400 mt-1">
                            Domain: {company.domain}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Assessments List */}
                  <div className="divide-y divide-gray-200">
                    {assessments[company.companyId] && assessments[company.companyId].length === 0 ? (
                      <div className="px-6 py-8 text-center text-gray-500">
                        No assessments available for this company.
                      </div>
                    ) : assessments[company.companyId] && assessments[company.companyId].length > 0 ? (
                      assessments[company.companyId].map((assessment) => (
                        <div key={assessment.assessmentId || assessment.id} className="px-6 py-4 hover:bg-gray-50">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center">
                              <div className="flex-shrink-0">
                                <DocumentTextIcon className="h-6 w-6 text-blue-500" />
                              </div>
                              <div className="ml-4">
                                <div className="text-sm font-medium text-gray-900">
                                  {assessment.name || assessment.title || 'Untitled Assessment'}
                                </div>
                                {assessment.description && (
                                  <div className="text-sm text-gray-500 mt-1">
                                    {assessment.description}
                                  </div>
                                )}
                                <div className="text-xs text-gray-400 mt-1 space-y-1">
                                  {assessment.duration && (
                                    <div>Duration: {assessment.duration} minutes</div>
                                  )}
                                  {assessment.createdBy && (
                                    <div>Created by: {assessment.createdBy}</div>
                                  )}
                                  {(assessment.assessmentId || assessment.id) && (
                                    <div>Assessment ID: {assessment.assessmentId || assessment.id}</div>
                                  )}
                                </div>
                              </div>
                            </div>
                            <div className="flex items-center">
                              <button
                                onClick={() => handleAttemptAssessment(assessment.assessmentId || assessment.id)}
                                className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                              >
                                <PlayIcon className="h-4 w-4 mr-2" />
                                Attempt
                              </button>
                            </div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="px-6 py-8 text-center">
                        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
                        <p className="text-sm text-gray-500 mt-2">Loading assessments...</p>
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Mobile bottom navigation */}
      <div className="fixed bottom-0 inset-x-0 bg-white border-t border-gray-200 sm:hidden">
        <div className="flex justify-around">
          <Link to="/dashboard" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <span className="mt-1">Home</span>
          </Link>
          <Link to="/companies" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-indigo-600">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span className="mt-1">Assessments</span>
          </Link>
          <a href="#" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <span className="mt-1">Practice</span>
          </a>
          <a href="#" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            <span className="mt-1">Progress</span>
          </a>
          <a href="#" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <span className="mt-1">Settings</span>
          </a>
        </div>
      </div>
    </div>
  );
};

export default Companies;
