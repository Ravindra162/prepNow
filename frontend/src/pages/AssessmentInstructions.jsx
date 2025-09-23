import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { assessmentService } from '../utils/adminApi';
import {
  ExclamationTriangleIcon,
  CameraIcon,
  ClockIcon,
  DocumentTextIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';

const AssessmentInstructions = () => {
  const { assessmentId } = useParams();
  const navigate = useNavigate();
  const [assessment, setAssessment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cameraPermission, setCameraPermission] = useState(false);
  const [agreementChecked, setAgreementChecked] = useState(false);

  useEffect(() => {
    fetchAssessmentDetails();
    checkCameraPermission();
  }, [assessmentId]);

  const fetchAssessmentDetails = async () => {
    try {
      setLoading(true);
      const data = await assessmentService.getAssessmentById(assessmentId);
      setAssessment(data);
    } catch (error) {
      console.error('Error fetching assessment:', error);
      toast.error('Failed to load assessment details');
      navigate('/companies');
    } finally {
      setLoading(false);
    }
  };

  const checkCameraPermission = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      setCameraPermission(true);
      // Stop the stream immediately after checking
      stream.getTracks().forEach(track => track.stop());
    } catch (error) {
      console.error('Camera permission denied:', error);
      setCameraPermission(false);
    }
  };

  const requestCameraPermission = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      setCameraPermission(true);
      toast.success('Camera permission granted');
      // Stop the stream immediately after checking
      stream.getTracks().forEach(track => track.stop());
    } catch (error) {
      console.error('Camera permission denied:', error);
      toast.error('Camera permission is required to start the assessment');
    }
  };

  const startAssessment = () => {
    if (!cameraPermission) {
      toast.error('Please allow camera access to proceed');
      return;
    }
    if (!agreementChecked) {
      toast.error('Please agree to the terms and conditions');
      return;
    }
    navigate(`/assessment/${assessmentId}/test`);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!assessment) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Assessment not found</h2>
          <button
            onClick={() => navigate('/companies')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Back to Companies
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center">
            <DocumentTextIcon className="h-8 w-8 text-blue-500" />
            <div className="ml-4">
              <h1 className="text-2xl font-bold text-gray-900">
                {assessment.name || assessment.title}
              </h1>
              <div className="flex items-center mt-2 text-sm text-gray-500">
                <ClockIcon className="h-4 w-4 mr-1" />
                Duration: {assessment.duration} minutes
              </div>
            </div>
          </div>
          {assessment.description && (
            <p className="mt-4 text-gray-600">{assessment.description}</p>
          )}
        </div>

        {/* Instructions */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Assessment Instructions</h2>
          <div className="space-y-4 text-gray-700">
            <div className="flex items-start">
              <ExclamationTriangleIcon className="h-5 w-5 text-amber-500 mt-0.5 mr-3 flex-shrink-0" />
              <div>
                <p className="font-medium">Read all instructions carefully before starting</p>
                <p className="text-sm text-gray-600">Make sure you understand all the requirements and guidelines.</p>
              </div>
            </div>
            
            <div className="flex items-start">
              <ClockIcon className="h-5 w-5 text-blue-500 mt-0.5 mr-3 flex-shrink-0" />
              <div>
                <p className="font-medium">Time Management</p>
                <p className="text-sm text-gray-600">
                  You have {assessment.duration} minutes to complete this assessment. 
                  The timer will start once you begin the test.
                </p>
              </div>
            </div>

            <div className="flex items-start">
              <CameraIcon className="h-5 w-5 text-green-500 mt-0.5 mr-3 flex-shrink-0" />
              <div>
                <p className="font-medium">Camera Monitoring</p>
                <p className="text-sm text-gray-600">
                  Your camera will be used to monitor the assessment for security purposes. 
                  Please ensure good lighting and keep your face visible.
                </p>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-200 rounded-md p-4">
              <h3 className="font-medium text-amber-800 mb-2">Important Guidelines:</h3>
              <ul className="text-sm text-amber-700 space-y-1">
                <li>• Do not refresh the page during the assessment</li>
                <li>• Do not switch tabs or applications</li>
                <li>• Ensure stable internet connection</li>
                <li>• Use a quiet environment free from distractions</li>
                <li>• Answer all questions before submitting</li>
                <li>• Once submitted, you cannot change your answers</li>
              </ul>
            </div>
          </div>
        </div>

        {/* System Check */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">System Check</h2>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <CameraIcon className="h-5 w-5 text-gray-500 mr-3" />
                <span className="text-gray-700">Camera Access</span>
              </div>
              <div className="flex items-center">
                {cameraPermission ? (
                  <>
                    <CheckCircleIcon className="h-5 w-5 text-green-500 mr-2" />
                    <span className="text-green-600 text-sm">Granted</span>
                  </>
                ) : (
                  <button
                    onClick={requestCameraPermission}
                    className="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    Allow Camera
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Agreement and Start */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-start mb-6">
            <input
              type="checkbox"
              id="agreement"
              checked={agreementChecked}
              onChange={(e) => setAgreementChecked(e.target.checked)}
              className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label htmlFor="agreement" className="ml-3 text-sm text-gray-700">
              I have read and understood all the instructions. I agree to the terms and conditions 
              and understand that this assessment is being monitored for security purposes.
            </label>
          </div>

          <div className="flex justify-between">
            <button
              onClick={() => navigate('/companies')}
              className="px-6 py-3 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              onClick={startAssessment}
              disabled={!cameraPermission || !agreementChecked}
              className="px-8 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-semibold"
            >
              Start Assessment
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AssessmentInstructions;
