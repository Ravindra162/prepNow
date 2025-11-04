import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { assessmentService } from '../utils/adminApi';

const Dashboard = () => {
  const { currentUser } = useAuth();
  const [dashboardData, setDashboardData] = useState({
    stats: {
      totalAssessments: 0,
      evaluated: 0,
      inProgress: 0
    },
    recentAssessments: []
  });
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, [currentUser]);

  const getUserRef = () => {
    if (!currentUser || !currentUser.email) {
      return 0;
    }

    // Create a simple numeric hash from email (same as MyTests page)
    let hash = 0;
    for (let i = 0; i < currentUser.email.length; i++) {
      const char = currentUser.email.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash);
  };

  const formatAssessmentName = (name) => {
    if (!name) return 'Assessment';

    // Replace underscores with spaces and capitalize each word
    return name
      .replace(/_/g, ' ')
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  };

  const fetchDashboardData = async () => {
    if (!currentUser?.email) {
      setLoadingData(false);
      return;
    }

    try {
      setLoadingData(true);
      const userRef = getUserRef();

      // Fetch user's assessment attempts using the same API as MyTests
      const candidates = await assessmentService.getUserAttemptedAssessments(userRef);

      // Calculate stats
      const totalAssessments = candidates.length;
      const evaluated = candidates.filter(c => c.status === 'EVALUATED').length;
      const inProgress = candidates.filter(c => c.status === 'IN_PROGRESS' || c.status === 'SUBMITTED').length;

      // Get top 3 recent assessments
      const recentAssessments = candidates.slice(0, 3).map(candidate => ({
        id: candidate.id,
        name: candidate.assessmentName || formatAssessmentName(candidate.assessment?.name) || 'Assessment',
        company: candidate.companyName || candidate.assessment?.company?.name || 'N/A',
        type: candidate.assessment?.type || 'Mixed',
        date: new Date(candidate.createdAt).toLocaleDateString(),
        score: candidate.status === 'EVALUATED' && candidate.totalScore !== null && candidate.maxScore !== null
          ? `${candidate.totalScore}/${candidate.maxScore} (${candidate.percentageScore?.toFixed(1)}%)`
          : 'Pending',
        status: candidate.status,
        percentageScore: candidate.percentageScore
      }));

      setDashboardData({
        stats: {
          totalAssessments,
          evaluated,
          inProgress
        },
        recentAssessments
      });
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      toast.error('Failed to load dashboard data');
    } finally {
      setLoadingData(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'EVALUATED': { color: 'bg-green-100 text-green-800', text: 'Evaluated' },
      'IN_PROGRESS': { color: 'bg-yellow-100 text-yellow-800', text: 'In Progress' },
      'SUBMITTED': { color: 'bg-blue-100 text-blue-800', text: 'Submitted' },
      'NOT_STARTED': { color: 'bg-gray-100 text-gray-800', text: 'Not Started' }
    };

    const config = statusConfig[status] || statusConfig['NOT_STARTED'];
    return (
      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${config.color}`}>
        {config.text}
      </span>
    );
  };

  const stats = [
    { name: 'Total Assessments', value: loadingData ? '...' : dashboardData.stats.totalAssessments.toString() },
    { name: 'Evaluated', value: loadingData ? '...' : dashboardData.stats.evaluated.toString() },
    { name: 'Pending', value: loadingData ? '...' : (dashboardData.stats.totalAssessments - dashboardData.stats.evaluated).toString() },
  ];

  return (
    <div className="py-6">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="mt-2 text-sm text-gray-600">
            Welcome back, {currentUser?.name || 'User'}! Here's your progress so far.
          </p>
        </div>

        {/* Stats */}
        <div className="mt-8">
          <dl className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {stats.map((item) => (
              <div key={item.name} className="px-4 py-5 bg-white shadow rounded-lg overflow-hidden sm:p-6">
                <dt className="text-sm font-medium text-gray-500 truncate">{item.name}</dt>
                <dd className="mt-1 text-3xl font-semibold text-gray-900">{item.value}</dd>
              </div>
            ))}
          </dl>
        </div>

        <div className="mt-8">
          <div className="mb-4">
            <h2 className="text-lg font-medium text-gray-900">Continue Practicing</h2>
            <p className="mt-1 text-sm text-gray-500">Pick up where you left off or start a new assessment.</p>
          </div>

          {/* Recent Assessments */}
          <div className="mt-6">
            <div className="mb-4">
              <h3 className="text-md font-medium text-gray-900">Recent Assessments</h3>
            </div>
            <div className="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
              {loadingData ? (
                <div className="bg-white px-6 py-12 text-center">
                  <p className="text-gray-500">Loading your assessments...</p>
                </div>
              ) : dashboardData.recentAssessments.length === 0 ? (
                <div className="bg-white px-6 py-12 text-center">
                  <p className="text-gray-500">No assessments yet. Start your first assessment!</p>
                  <Link
                    to="/companies"
                    className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700"
                  >
                    Browse Assessments
                  </Link>
                </div>
              ) : (
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Assessment Name
                      </th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Date
                      </th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Score
                      </th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {dashboardData.recentAssessments.map((assessment) => (
                      <tr key={assessment.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10 flex items-center justify-center rounded-full bg-indigo-100">
                              <span className="text-indigo-600 font-medium">{assessment.name[0]?.toUpperCase()}</span>
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">{assessment.name}</div>
                              <div className="text-sm text-gray-500">{assessment.company}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {assessment.date}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          {assessment.status === 'EVALUATED' ? (
                            <span className={`${assessment.percentageScore >= 80 ? 'text-green-600' : assessment.percentageScore >= 60 ? 'text-yellow-600' : 'text-red-600'}`}>
                              {assessment.score}
                            </span>
                          ) : (
                            <span className="text-gray-400">{assessment.score}</span>
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(assessment.status)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="mt-8">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Link
              to="/companies"
              className="relative rounded-lg border border-gray-300 bg-white px-6 py-5 shadow-sm flex items-center space-x-3 hover:border-indigo-400 focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-indigo-500"
            >
              <div className="flex-shrink-0">
                <svg className="h-10 w-10 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <div className="flex-1 min-w-0">
                <span className="absolute inset-0" aria-hidden="true" />
                <p className="text-sm font-medium text-gray-900">Browse Companies</p>
                <p className="text-sm text-gray-500 truncate">Explore assessments by company</p>
              </div>
            </Link>

            <Link
              to="/my-tests"
              className="relative rounded-lg border border-gray-300 bg-white px-6 py-5 shadow-sm flex items-center space-x-3 hover:border-indigo-400 focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-indigo-500"
            >
              <div className="flex-shrink-0">
                <svg className="h-10 w-10 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div className="flex-1 min-w-0">
                <span className="absolute inset-0" aria-hidden="true" />
                <p className="text-sm font-medium text-gray-900">My Tests</p>
                <p className="text-sm text-gray-500 truncate">View all your test attempts</p>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

