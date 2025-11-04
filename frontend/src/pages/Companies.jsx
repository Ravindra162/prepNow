import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';
import { assessmentService } from '../utils/adminApi';
import {
  BuildingOfficeIcon,
  DocumentTextIcon,
  PlayIcon,
} from '@heroicons/react/24/outline';

const Companies = () => {
  const { currentUser } = useAuth();
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [assessments, setAssessments] = useState({});
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

  const handleAttemptAssessment = (assessmentId) => {
    navigate(`/assessment/${assessmentId}/instructions`);
  };

  return (
    <div className="py-6">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Companies & Assessments</h1>
          <p className="text-gray-600 mt-2">Explore available assessments by company</p>
        </div>

        {/* Companies List */}
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
  );
};

export default Companies;
