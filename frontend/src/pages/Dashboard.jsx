import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

const Dashboard = () => {
  const { currentUser, logout } = useAuth();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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
    { name: 'Total Assessments', value: '12' },
    { name: 'Completed', value: '8' },
    { name: 'In Progress', value: '3' },
    { name: 'High Score', value: '92%' },
  ];

  const recentAssessments = [
    { id: 1, name: 'Google Coding Challenge', company: 'Google', type: 'Coding', date: '2023-05-15', score: '85%' },
    { id: 2, name: 'Amazon System Design', company: 'Amazon', type: 'System Design', date: '2023-05-10', score: '78%' },
    { id: 3, name: 'Facebook Algorithms', company: 'Meta', type: 'Algorithms', date: '2023-05-05', score: '92%' },
  ];

  const recommendedTests = [
    { id: 1, name: 'Microsoft Azure', type: 'Cloud Services', difficulty: 'Medium', questions: 20 },
    { id: 2, name: 'Netflix System Design', type: 'System Design', difficulty: 'Hard', questions: 5 },
    { id: 3, name: 'Apple iOS Development', type: 'Mobile', difficulty: 'Medium', questions: 15 },
  ];

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center
">
              <div className="flex-shrink-0 flex items-center">
                <span className="text-xl font-bold text-indigo-600">PrepNow</span>
              </div>
              <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
                <Link to="/dashboard" className="border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Dashboard
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
              <dl className="mt-5 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
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
                        <table className="min-w-full divide-y divide-gray-200">
                          <thead className="bg-gray-50">
                            <tr>
                              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Assessment
                              </th>
                              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Type
                              </th>
                              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Date
                              </th>
                              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Score
                              </th>
                              <th scope="col" className="relative px-6 py-3">
                                <span className="sr-only">Action</span>
                              </th>
                            </tr>
                          </thead>
                          <tbody className="bg-white divide-y divide-gray-200">
                            {recentAssessments.map((assessment) => (
                              <tr key={assessment.id}>
                                <td className="px-6 py-4 whitespace-nowrap">
                                  <div className="flex items-center">
                                    <div className="flex-shrink-0 h-10 w-10 flex items-center justify-center rounded-full bg-indigo-100">
                                      <span className="text-indigo-600 font-medium">{assessment.company[0]}</span>
                                    </div>
                                    <div className="ml-4">
                                      <div className="text-sm font-medium text-gray-900">{assessment.name}</div>
                                      <div className="text-sm text-gray-500">{assessment.company}</div>
                                    </div>
                                  </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                  <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                                    {assessment.type}
                                  </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                  {assessment.date}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                  <span className={`${parseInt(assessment.score) > 80 ? 'text-green-600' : 'text-yellow-600'}`}>
                                    {assessment.score}
                                  </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                  <a href="#" className="text-indigo-600 hover:text-indigo-900">View</a>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Recommended Tests */}
              <div className="mt-8">
                <div className="px-4 sm:px-0">
                  <h3 className="text-md font-medium text-gray-900">Recommended for You</h3>
                  <p className="mt-1 text-sm text-gray-500">Based on your previous assessments and skill level.</p>
                </div>
                <div className="mt-4 grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
                  {recommendedTests.map((test) => (
                    <div key={test.id} className="bg-white overflow-hidden shadow rounded-lg">
                      <div className="px-4 py-5 sm:p-6">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 bg-indigo-500 rounded-md p-3">
                            <svg className="h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                          </div>
                          <div className="ml-5 w-0 flex-1">
                            <dl>
                              <dt className="text-sm font-medium text-gray-500 truncate">{test.type}</dt>
                              <dd>
                                <div className="text-lg font-medium text-gray-900">{test.name}</div>
                              </dd>
                            </dl>
                          </div>
                        </div>
                        <div className="mt-4 flex justify-between items-center">
                          <div>
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                              {test.difficulty}
                            </span>
                            <span className="ml-2 text-sm text-gray-500">{test.questions} questions</span>
                          </div>
                          <button
                            type="button"
                            className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                          >
                            Start Test
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>

      {/* Mobile bottom navigation */}
      <div className="fixed bottom-0 inset-x-0 bg-white border-t border-gray-200 sm:hidden">
        <div className="flex justify-around">
          <a href="#" className="w-full flex flex-col items-center justify-center px-4 py-2 text-sm font-medium text-indigo-600">
            <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
            <span className="mt-1">Home</span>
          </a>
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
