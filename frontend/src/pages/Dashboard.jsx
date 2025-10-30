import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { assessmentService } from '../utils/adminApi';

const Dashboard = () => {
  const { currentUser, logout } = useAuth();
  const [loading, setLoading] = useState(false);
  const [dashboardData, setDashboardData] = useState({
    stats: {
      totalAssessments: 0,
      evaluated: 0,
      inProgress: 0
    },
    recentAssessments: []
  });
  const [loadingData, setLoadingData] = useState(true);
  const navigate = useNavigate();

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
        // Use assessmentName if available (formatted), otherwise format the assessment.name
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

  const handleLogout = async () => {
    try {
      setLoading(true);
      await logout();
      toast.success('Successfully logged out');
      navigate('/login');
    } catch (error) {
      toast.error('Failed to log out');
    } finally {
      setLoading(false);
    }
  };

  const stats = [
    { name: 'Total Assessments', value: loadingData ? '...' : dashboardData.stats.totalAssessments.toString() },
    { name: 'Evaluated', value: loadingData ? '...' : dashboardData.stats.evaluated.toString() },
    { name: 'Pending', value: loadingData ? '...' : dashboardData.stats.totalAssessments.toString() -  dashboardData.stats.evaluated.toString()},
  ];

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
                <Link to="/dashboard" className="border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Dashboard
                </Link>
                <Link to="/companies" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Assessments
                </Link>
                <a href="#" className="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Practice
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
                disabled={loading}
                className="ml-4 px-4 py-2 border border-transparent text-sm font-medium rounded-md text-indigo-700 bg-indigo-100 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Signing out...' : 'Sign out'}
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="py-10">
        <header>
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
            <p className="mt-1 text-sm text-gray-500">
              Welcome back, {currentUser?.name || 'User'}! Here's your progress so far.
            </p>
          </div>
        </header>
        <main>
          <div className="max-w-7xl mx-auto sm:px-6 lg:px-8">
            {/* Stats */}
            <div className="px-4 mt-8 sm:px-0">
              <dl className="mt-5 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
                {stats.map((item) => (
                  <div key={item.name} className="px-4 py-5 bg-white shadow rounded-lg overflow-hidden sm:p-6">
                    <dt className="text-sm font-medium text-gray-500 truncate">{item.name}</dt>
                    <dd className="mt-1 text-3xl font-semibold text-gray-900">{item.value}</dd>
                  </div>
                ))}
              </dl>
            </div>

            <div className="mt-8">
              <div className="px-4 sm:px-0">
                <h2 className="text-lg font-medium text-gray-900">Continue Practicing</h2>
                <p className="mt-1 text-sm text-gray-500">Pick up where you left off or start a new assessment.</p>
              </div>

              {/* Recent Assessments */}
              <div className="mt-6">
                <div className="px-4 sm:px-0">
                  <h3 className="text-md font-medium text-gray-900">Recent Assessments</h3>
                </div>
                <div className="mt-4 flex flex-col">
                  <div className="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                    <div className="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
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
                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                      assessment.status === 'EVALUATED'
                                        ? 'bg-green-100 text-green-800'
                                        : assessment.status === 'IN_PROGRESS'
                                        ? 'bg-yellow-100 text-yellow-800'
                                        : 'bg-blue-100 text-blue-800'
                                    }`}>
                                      {assessment.status.replace('_', ' ')}
                                    </span>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>

      {/* Mobile bottom navigation */}
      <div className="fixed bottom-0 inset-x-0 bg-white border-t border-gray-200 sm:hidden">
        <div className="flex justify-around">
          <Link to="/dashboard" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-indigo-600">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <span className="mt-1">Home</span>
          </Link>
          <Link to="/companies" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700">
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

export default Dashboard;

