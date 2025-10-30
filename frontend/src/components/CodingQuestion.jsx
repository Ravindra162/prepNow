import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import Split from 'react-split';
import './CodingQuestion.css';
import {
  PlayIcon,
  CheckCircleIcon,
  XCircleIcon,
  ChevronDownIcon,
  ChevronUpIcon
} from '@heroicons/react/24/outline';

const CodingQuestion = ({ question, answer, onAnswerChange }) => {
  const [code, setCode] = useState(answer || '');
  const [language, setLanguage] = useState('javascript');
  const [testResults, setTestResults] = useState([]);
  const [showTestCases, setShowTestCases] = useState(true);
  const [isRunning, setIsRunning] = useState(false);

  // Update code when answer prop changes
  useEffect(() => {
    if (answer !== undefined && answer !== code) {
      setCode(answer);
    }
  }, [answer]);

  // Extract language from question if specified
  useEffect(() => {
    if (question?.language) {
      setLanguage(question.language.toLowerCase());
    } else if (question?.allowedLanguages && question.allowedLanguages.length > 0) {
      setLanguage(question.allowedLanguages[0].toLowerCase());
    }
  }, [question]);

  const handleCodeChange = (value) => {
    setCode(value || '');
    onAnswerChange(value || '');
  };

  const handleRunCode = async () => {
    setIsRunning(true);

    try {
      // Run the code against test cases
      if (question?.testCases && question.testCases.length > 0) {
        const results = await Promise.all(
          question.testCases.map(async (testCase, idx) => {
            try {
              const response = await fetch('http://localhost:8083/api/code/run', {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                  language: language,
                  code: code,
                  stdin: testCase.input || '',
                  runTimeout: 3000,
                }),
              });

              const result = await response.json();

              const actualOutput = result.run?.stdout?.trim() || '';
              const expectedOutput = (testCase.expectedOutput || '').trim();
              const passed = actualOutput === expectedOutput && result.run?.code === 0;

              return {
                id: idx,
                input: testCase.input,
                expectedOutput: expectedOutput,
                actualOutput: actualOutput,
                passed: passed,
                executionTime: 0, // Piston doesn't provide execution time
                error: result.run?.stderr || null
              };
            } catch (error) {
              console.error(`Error running test case ${idx}:`, error);
              return {
                id: idx,
                input: testCase.input,
                expectedOutput: testCase.expectedOutput,
                actualOutput: '',
                passed: false,
                executionTime: 0,
                error: error.message
              };
            }
          })
        );

        setTestResults(results);
        console.log('Test results:', results);
      } else {
        // No test cases, just run the code once
        const response = await fetch('http://localhost:8083/api/code/run', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            language: language,
            code: code,
            stdin: '',
            runTimeout: 3000,
          }),
        });

        const result = await response.json();
        console.log('Execution result:', result);

        if (result.run?.code === 0) {
          alert(`Output:\n${result.run.stdout}`);
        } else {
          alert(`Error:\n${result.run?.stderr || 'Unknown error'}`);
        }
      }

      setIsRunning(false);
      setShowTestCases(true);
    } catch (error) {
      console.error('Error executing code:', error);
      alert('Failed to execute code. Please try again.');
      setIsRunning(false);
    }
  };

  const getLanguageFromExtension = (lang) => {
    const langMap = {
      'javascript': 'javascript',
      'java': 'java',
      'python': 'python',
      'cpp': 'cpp',
      'c': 'c',
      'csharp': 'csharp',
      'go': 'go',
      'rust': 'rust',
    };
    return langMap[lang?.toLowerCase()] || 'javascript';
  };

  return (
    <div className="h-full flex flex-col">
      <Split
        className="flex h-full"
        sizes={[40, 60]}
        minSize={[300, 400]}
        gutterSize={8}
        gutterAlign="center"
        direction="horizontal"
        cursor="col-resize"
      >
        {/* Left Panel - Question Description */}
        <div className="flex flex-col h-full overflow-hidden border-r border-gray-200">
          <div className="flex-1 overflow-y-auto p-6">
            <div className="prose max-w-none">
              <h2 className="text-xl font-bold text-gray-900 mb-4">
                Problem Description
              </h2>

              <div className="text-gray-700 mb-6 whitespace-pre-wrap">
                {question?.questionText || question?.description}
              </div>

              {question?.constraints && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    Constraints
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <pre className="text-sm text-gray-700 whitespace-pre-wrap">
                      {question.constraints}
                    </pre>
                  </div>
                </div>
              )}

              {question?.examples && question.examples.length > 0 && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    Examples
                  </h3>
                  {question.examples.map((example, idx) => (
                    <div key={idx} className="bg-gray-50 p-4 rounded-lg mb-3">
                      <div className="mb-2">
                        <span className="font-semibold text-gray-700">Input:</span>
                        <pre className="mt-1 text-sm text-gray-600">
                          {example.input}
                        </pre>
                      </div>
                      <div>
                        <span className="font-semibold text-gray-700">Output:</span>
                        <pre className="mt-1 text-sm text-gray-600">
                          {example.output}
                        </pre>
                      </div>
                      {example.explanation && (
                        <div className="mt-2">
                          <span className="font-semibold text-gray-700">Explanation:</span>
                          <p className="mt-1 text-sm text-gray-600">
                            {example.explanation}
                          </p>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {question?.sampleInput && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    Sample Input
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <pre className="text-sm text-gray-700">
                      {question.sampleInput}
                    </pre>
                  </div>
                </div>
              )}

              {question?.sampleOutput && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">
                    Sample Output
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <pre className="text-sm text-gray-700">
                      {question.sampleOutput}
                    </pre>
                  </div>
                </div>
              )}

              {question?.difficultyLevel && (
                <div className="mb-4">
                  <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                    question.difficultyLevel === 'EASY' ? 'bg-green-100 text-green-800' :
                    question.difficultyLevel === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-red-100 text-red-800'
                  }`}>
                    {question.difficultyLevel}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Panel - Code Editor and Test Cases */}
        <div className="flex flex-col h-full overflow-hidden">
          {/* Editor Header */}
          <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
            <div className="flex items-center space-x-4">
              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                disabled={question?.allowedLanguages && question.allowedLanguages.length === 1}
              >
                {question?.allowedLanguages && question.allowedLanguages.length > 0 ? (
                  question.allowedLanguages.map((lang) => (
                    <option key={lang} value={lang.toLowerCase()}>
                      {lang}
                    </option>
                  ))
                ) : (
                  <>
                    <option value="javascript">JavaScript</option>
                    <option value="python">Python</option>
                    <option value="java">Java</option>
                    <option value="cpp">C++</option>
                    <option value="c">C</option>
                  </>
                )}
              </select>
              <span className="text-sm text-gray-600">
                {code.length} characters
              </span>
            </div>
            <button
              onClick={handleRunCode}
              disabled={isRunning || !code.trim()}
              className="flex items-center px-4 py-1.5 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <PlayIcon className="h-4 w-4 mr-2" />
              {isRunning ? 'Running...' : 'Run Code'}
            </button>
          </div>

          {/* Code Editor */}
          <div className="flex-1 overflow-hidden">
            <Editor
              height="100%"
              language={getLanguageFromExtension(language)}
              value={code}
              onChange={handleCodeChange}
              theme="vs-dark"
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                lineNumbers: 'on',
                scrollBeyondLastLine: false,
                automaticLayout: true,
                tabSize: 2,
                wordWrap: 'on',
                padding: { top: 10, bottom: 10 },
              }}
            />
          </div>

          {/* Test Cases Section */}
          {question?.testCases && question.testCases.length > 0 && (
            <div className="border-t border-gray-200">
              <div
                className="flex items-center justify-between px-4 py-2 bg-gray-50 cursor-pointer hover:bg-gray-100"
                onClick={() => setShowTestCases(!showTestCases)}
              >
                <h3 className="text-sm font-semibold text-gray-900">
                  Test Cases ({question.testCases.length})
                </h3>
                {showTestCases ? (
                  <ChevronDownIcon className="h-4 w-4 text-gray-600" />
                ) : (
                  <ChevronUpIcon className="h-4 w-4 text-gray-600" />
                )}
              </div>

              {showTestCases && (
                <div className="max-h-60 overflow-y-auto bg-white">
                  {testResults.length > 0 ? (
                    <div className="divide-y divide-gray-200">
                      {testResults.map((result, idx) => (
                        <div key={idx} className="p-4">
                          <div className="flex items-center justify-between mb-2">
                            <span className="text-sm font-medium text-gray-900">
                              Test Case {idx + 1}
                            </span>
                            <div className="flex items-center space-x-2">
                              {result.passed ? (
                                <CheckCircleIcon className="h-5 w-5 text-green-600" />
                              ) : (
                                <XCircleIcon className="h-5 w-5 text-red-600" />
                              )}
                              <span className={`text-sm font-medium ${
                                result.passed ? 'text-green-600' : 'text-red-600'
                              }`}>
                                {result.passed ? 'Passed' : 'Failed'}
                              </span>
                              <span className="text-xs text-gray-500">
                                ({result.executionTime}ms)
                              </span>
                            </div>
                          </div>
                          <div className="space-y-2 text-sm">
                            <div>
                              <span className="font-medium text-gray-700">Input:</span>
                              <pre className="mt-1 p-2 bg-gray-50 rounded text-xs">
                                {result.input}
                              </pre>
                            </div>
                            <div>
                              <span className="font-medium text-gray-700">Expected Output:</span>
                              <pre className="mt-1 p-2 bg-gray-50 rounded text-xs">
                                {result.expectedOutput}
                              </pre>
                            </div>
                            {!result.passed && (
                              <div>
                                <span className="font-medium text-gray-700">Your Output:</span>
                                <pre className="mt-1 p-2 bg-red-50 rounded text-xs">
                                  {result.actualOutput}
                                </pre>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="p-4">
                      <div className="divide-y divide-gray-200">
                        {question.testCases.slice(0, 3).map((testCase, idx) => (
                          <div key={idx} className="py-3">
                            <div className="text-sm font-medium text-gray-900 mb-2">
                              Test Case {idx + 1}
                            </div>
                            <div className="space-y-2 text-sm">
                              <div>
                                <span className="font-medium text-gray-700">Input:</span>
                                <pre className="mt-1 p-2 bg-gray-50 rounded text-xs overflow-x-auto">
                                  {testCase.input}
                                </pre>
                              </div>
                              {testCase.isHidden ? (
                                <div className="text-gray-500 italic text-xs">
                                  Expected output is hidden
                                </div>
                              ) : (
                                <div>
                                  <span className="font-medium text-gray-700">Expected Output:</span>
                                  <pre className="mt-1 p-2 bg-gray-50 rounded text-xs overflow-x-auto">
                                    {testCase.expectedOutput}
                                  </pre>
                                </div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                      {question.testCases.length > 3 && (
                        <div className="text-xs text-gray-500 mt-2">
                          + {question.testCases.length - 3} more hidden test cases
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </Split>
    </div>
  );
};

export default CodingQuestion;
