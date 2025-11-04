import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { assessmentService } from '../utils/adminApi';
import { useAuth } from '../contexts/AuthContext';
import {
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  CalendarIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';

const MyTests = () => {
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const [attempts, setAttempts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }
    fetchAttemptedTests();
  }, [currentUser]);

  const fetchAttemptedTests = async () => {
    try {
      setLoading(true);
      const userRef = getUserRef();

      // Fetch user's attempted assessments
      const attemptedTests = await assessmentService.getUserAttemptedAssessments(userRef);
      setAttempts(attemptedTests);
    } catch (error) {
      console.error('Error fetching attempted tests:', error);
      toast.error('Failed to load your test attempts');
    } finally {
      setLoading(false);
    }
  };

  const getUserRef = () => {
    if (!currentUser || !currentUser.email) {
      return 0;
    }

    // Create a simple numeric hash from email
    let hash = 0;
    for (let i = 0; i < currentUser.email.length; i++) {
      const char = currentUser.email.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash);
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      COMPLETED: {
        color: 'bg-green-100 text-green-800',
        icon: CheckCircleIcon,
        label: 'Completed',
      },
      IN_PROGRESS: {
        color: 'bg-yellow-100 text-yellow-800',
        icon: ClockIcon,
        label: 'In Progress',
      },
      EVALUATED: {
        color: 'bg-blue-100 text-blue-800',
        icon: ChartBarIcon,
        label: 'Evaluated',
      },
      INVITED: {
        color: 'bg-gray-100 text-gray-800',
        icon: CalendarIcon,
        label: 'Invited',
      },
    };

    const config = statusConfig[status] || statusConfig.INVITED;
    const Icon = config.icon;

    return (
      <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
        <Icon className="h-4 w-4 mr-1" />
        {config.label}
      </span>
    );
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatDuration = (minutes) => {
    if (!minutes) return 'N/A';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

  if (loading) {
    return (
      <div className="py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-center h-64">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-gray-600">Loading your test attempts...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="py-6">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">My Test Attempts</h1>
          <p className="text-sm text-gray-600 mt-2">
            View all your attempted assessments and scores
          </p>
        </div>

        {/* Content */}
        {attempts.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <ChartBarIcon className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No test attempts yet</h3>
            <p className="text-gray-600 mb-6">
              Start taking assessments to see your results here
            </p>
            <button
              onClick={() => navigate('/companies')}
              className="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Browse Companies
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {attempts.map((attempt) => (
              <div
                key={attempt.id}
                className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-4">
                        <div>
                          <h3 className="text-lg font-semibold text-gray-900">
                            {attempt.assessmentName || attempt.assessment?.name || attempt.assessment?.title || 'Assessment'}
                          </h3>
                          {attempt.companyName && (
                            <p className="text-sm text-gray-600 mt-1">
                              {attempt.companyName}
                            </p>
                          )}
                        </div>
                        {getStatusBadge(attempt.status)}
                      </div>

                      {/* Test Stats Grid */}
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                        {/* Started At */}
                        <div>
                          <p className="text-sm text-gray-500">Started</p>
                          <p className="text-sm font-medium text-gray-900">
                            {formatDate(attempt.createdAt)}
                          </p>
                        </div>

                        {/* Duration */}
                        {attempt.timeTaken && (
                          <div>
                            <p className="text-sm text-gray-500">Time Taken</p>
                            <p className="text-sm font-medium text-gray-900">
                              {formatDuration(attempt.timeTaken)}
                            </p>
                          </div>
                        )}

                        {/* Score */}
                        {attempt.status === 'EVALUATED' && attempt.totalScore !== null && (
                          <div>
                            <p className="text-sm text-gray-500">Score</p>
                            <p className="text-sm font-medium text-gray-900">
                              {attempt.totalScore}/{attempt.maxScore || 'N/A'}
                            </p>
                          </div>
                        )}

                        {/* Percentage */}
                        {attempt.status === 'EVALUATED' && attempt.percentageScore !== null && (
                          <div>
                            <p className="text-sm text-gray-500">Percentage</p>
                            <p className={`text-sm font-medium ${
                              attempt.percentageScore >= 80
                                ? 'text-green-600'
                                : attempt.percentageScore >= 60
                                ? 'text-yellow-600'
                                : 'text-red-600'
                            }`}>
                              {attempt.percentageScore.toFixed(1)}%
                            </p>
                          </div>
                        )}
                      </div>

                      {/* Additional Info */}
                      {attempt.status === 'EVALUATED' && (
                        <div className="mt-4 pt-4 border-t border-gray-200">
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">
                              Result: {attempt.percentageScore >= 60 ? (
                                <span className="text-green-600 font-medium">Passed ✓</span>
                              ) : (
                                <span className="text-red-600 font-medium">Not Passed ✗</span>
                              )}
                            </span>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyTests;

