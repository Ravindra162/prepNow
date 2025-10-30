# Submission Service - Progress Report

**Service Name:** SubmissionService  
**Port:** 8083  
**Database:** MongoDB  
**Last Updated:** October 24, 2025  

---

## Overview
The Submission Service manages assessment submissions, code execution, and evaluation. It handles file uploads, code compilation/execution, and automatic evaluation of MCQ and coding questions. Uses MongoDB for flexible document storage.

---

## Functionalities Implemented

### 1. Submission Management
**Endpoints:**
- `POST /submissions` - Create a new submission
- `GET /submissions/{id}` - Get submission by ID
- `GET /submissions/user/{userId}` - Get all submissions for a user
- `GET /submissions/test/{testId}` - Get all submissions for a test
- `PUT /submissions/{id}/status` - Update submission status
- `DELETE /submissions/{id}` - Delete submission

**Features:**
- Create submissions for assessments
- Track submission status (DRAFT, SUBMITTED, EVALUATING, EVALUATED)
- Store metadata and file references
- Link submissions to evaluations
- User and test filtering

---

### 2. Code Execution
**Endpoints:**
- `POST /execute` - Execute code with test cases
- `POST /execute/single` - Execute single code snippet

**Features:**
- Multi-language code execution (Java, Python, C++, JavaScript)
- Test case validation
- Timeout handling
- Memory limit enforcement
- Compilation error capture
- Runtime error capture
- Output comparison
- Execution time tracking

**Supported Languages:**
- ☑️ Java (OpenJDK)
- ☑️ Python 3
- ☑️ C++ (g++)
- ☑️ JavaScript (Node.js)

**Execution Request:**
```json
{
  "code": "def add(a, b):\n    return a + b\n\nprint(add(2, 3))",
  "language": "python",
  "input": "2\n3",
  "timeLimit": 5
}
```

**Execution Response:**
```json
{
  "output": "5\n",
  "executionTime": 0.123,
  "status": "SUCCESS",
  "error": null
}
```

---

### 3. Evaluation Service
**Endpoints:**
- `POST /evaluate` - Evaluate entire submission
- `GET /evaluations/{id}` - Get evaluation by ID
- `GET /evaluations/submission/{submissionId}` - Get evaluation for submission
- `PUT /evaluations/{id}` - Update evaluation

**Features:**
- Automatic MCQ evaluation
- Automatic coding question evaluation with test cases
- Comprehensive scoring breakdown
- Question-level results
- Performance analytics
- Score calculation with weighted sections
- Pass/fail determination
- Integration with AssessmentService for score updates

**Evaluation Request:**
```json
{
  "submissionId": "67890",
  "assessmentId": 123,
  "candidateId": 456,
  "userRef": 789,
  "answers": {
    "mcq": [
      {
        "questionId": 1,
        "selectedOption": 2,
        "correctOption": 2,
        "points": 10
      }
    ],
    "coding": [
      {
        "questionId": 101,
        "code": "public String reverse(String s) {...}",
        "language": "java",
        "testCases": [...]
      }
    ]
  }
}
```

**Evaluation Response:**
```json
{
  "id": "eval-123",
  "submissionId": "67890",
  "totalScore": 85.5,
  "maxScore": 100,
  "percentageScore": 85.5,
  "mcqScore": 40,
  "mcqMaxScore": 50,
  "mcqCorrect": 4,
  "mcqTotal": 5,
  "codingScore": 45.5,
  "codingMaxScore": 50,
  "codingPassed": 2,
  "codingTotal": 3,
  "passed": true,
  "passingThreshold": 60,
  "evaluatedAt": "2025-10-24T12:00:00Z",
  "questionResults": [...]
}
```

---

### 4. File Management
**Endpoints:**
- `POST /files/upload` - Upload code file
- `GET /files/{id}` - Get file by ID
- `GET /files/submission/{submissionId}` - Get all files for submission
- `DELETE /files/{id}` - Delete file

**Features:**
- Store code files for submissions
- Support multiple file uploads per submission
- File metadata tracking
- Content retrieval
- GridFS storage for large files

---

## Database Schema (MongoDB)

### Collections

#### 1. **submissions**
```javascript
{
  _id: ObjectId,
  userId: String,              // User identifier
  testId: String,              // Assessment/Test identifier
  createdAt: ISODate,
  submittedAt: ISODate,
  status: String,              // DRAFT, SUBMITTED, EVALUATING, EVALUATED
  totalScore: Double,
  maxScore: Double,
  metadata: {                  // Flexible metadata
    assessmentName: String,
    userName: String,
    // ... other metadata
  },
  fileIds: [String],           // References to submission_files
  evaluationId: String         // Reference to evaluation
}
```

**Status Enum:**
- `DRAFT` - Submission being prepared
- `SUBMITTED` - Submitted for evaluation
- `EVALUATING` - Currently being evaluated
- `EVALUATED` - Evaluation complete

**Indexes:**
- `userId` - For user submission lookup
- `testId` - For test submission lookup
- `status` - For filtering by status

---

#### 2. **evaluations**
```javascript
{
  _id: ObjectId,
  submissionId: String,        // Unique reference to submission
  totalScore: Double,
  maxScore: Double,
  percentageScore: Double,
  
  // MCQ Results
  mcqScore: Double,
  mcqMaxScore: Double,
  mcqCorrect: Integer,
  mcqTotal: Integer,
  
  // Coding Results
  codingScore: Double,
  codingMaxScore: Double,
  codingPassed: Integer,
  codingTotal: Integer,
  
  evaluatedAt: ISODate,
  evaluatorId: String,
  remarks: String,
  
  // Detailed breakdown
  breakdown: {
    sectionName: Double,       // Score per section
    // ...
  },
  
  detailedResults: {           // Additional details
    // ...
  },
  
  // Question-level results
  questionResults: [
    {
      questionId: Long,
      questionType: String,
      isCorrect: Boolean,
      score: Double,
      maxScore: Double,
      executionTime: Double,
      testCaseResults: [
        {
          input: String,
          expectedOutput: String,
          actualOutput: String,
          passed: Boolean
        }
      ]
    }
  ],
  
  passed: Boolean,
  passingThreshold: Double,
  
  // Statistics
  totalQuestionsAttempted: Integer,
  totalQuestionsCorrect: Integer,
  totalQuestionsIncorrect: Integer,
  totalQuestionsUnanswered: Integer
}
```

**Indexes:**
- Unique index on `submissionId`
- `passed` - For filtering passed/failed

---

#### 3. **submission_files**
```javascript
{
  _id: ObjectId,
  submissionId: String,
  fileName: String,
  fileType: String,
  content: String,             // Code content
  language: String,
  uploadedAt: ISODate,
  size: Long
}
```

**Indexes:**
- `submissionId` - For file lookup by submission

---

#### 4. **code_executions**
```javascript
{
  _id: ObjectId,
  code: String,
  language: String,
  input: String,
  output: String,
  error: String,
  executionTime: Double,
  status: String,              // SUCCESS, COMPILATION_ERROR, RUNTIME_ERROR, TIMEOUT
  createdAt: ISODate
}
```

**Status Enum:**
- `SUCCESS` - Code executed successfully
- `COMPILATION_ERROR` - Code failed to compile
- `RUNTIME_ERROR` - Code crashed during execution
- `TIMEOUT` - Execution exceeded time limit

---

## Code Execution Details

### Execution Environment
- **Docker Containers:** Isolated execution per language
- **Time Limit:** 5 seconds default (configurable)
- **Memory Limit:** 256MB (configurable)
- **Security:** Sandboxed execution, no network access

### Execution Process
```
1. Receive code and test cases
   ↓
2. Validate language support
   ↓
3. Create temporary file
   ↓
4. Compile (if needed - Java, C++)
   ↓
5. Execute with each test case
   ↓
6. Capture output and errors
   ↓
7. Compare with expected output
   ↓
8. Calculate score
   ↓
9. Clean up temporary files
```

### Language-Specific Commands
```bash
# Java
javac Solution.java
java Solution < input.txt

# Python
python3 solution.py < input.txt

# C++
g++ -o solution solution.cpp
./solution < input.txt

# JavaScript
node solution.js < input.txt
```

---

## Evaluation Process

### Automatic Evaluation Flow
```
1. Receive submission with answers
   ↓
2. Fetch questions from QuestionService
   ↓
3. Evaluate MCQ Questions
   - Compare selected vs correct option
   - Award points if correct
   ↓
4. Evaluate Coding Questions
   - Execute code with test cases
   - Check each test case pass/fail
   - Calculate partial scores
   ↓
5. Calculate total scores
   - MCQ score
   - Coding score
   - Total score
   - Percentage
   ↓
6. Determine pass/fail
   ↓
7. Store evaluation in MongoDB
   ↓
8. Update AssessmentService with scores
   ↓
9. Return evaluation results
```

### Scoring Logic
- **MCQ:** All or nothing per question
- **Coding:** Partial credit based on test cases passed
  - Example: 3/5 test cases = 60% of question points
- **Total Score:** Sum of all question scores
- **Percentage:** (totalScore / maxScore) × 100

---

## Integration Points

### With AssessmentService
- **Sends:** Score updates after evaluation
- **Receives:** Assessment structure and candidate info
- **Endpoint:** `PUT /assessments/candidates/{candidateId}/score`

### With QuestionService
- **Fetches:** Question details, test cases, correct answers
- **Used for:** Evaluation validation and scoring

### With Frontend
- **Code Editor:** Receives code submissions
- **Results Display:** Returns evaluation results
- **Real-time Execution:** Code testing during assessment

---

## Key Features

### ✅ Multi-Language Support
- Java, Python, C++, JavaScript
- Easy to add more languages
- Language-specific compilation and execution

### ✅ Comprehensive Evaluation
- MCQ auto-grading
- Coding test case validation
- Partial credit for coding questions
- Detailed result breakdown

### ✅ Security
- Sandboxed code execution
- Time and memory limits
- No network access
- Input validation

### ✅ Performance Tracking
- Execution time per test case
- Memory usage monitoring
- Compilation time tracking

### ✅ Flexible Storage
- MongoDB for schema flexibility
- Easy to add new evaluation metrics
- Fast document retrieval

---

## Technology Stack
- **Framework:** Spring Boot 3.x
- **Database:** MongoDB
- **Code Execution:** Process execution with Docker
- **Language Support:** Multiple compilers/interpreters
- **File Storage:** GridFS (for large files)
- **Integration:** RestTemplate for microservice calls

---

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8083

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/submission_db
spring.data.mongodb.database=submission_db

# Code Execution Configuration
code.execution.timeout=5000
code.execution.memory-limit=268435456
code.execution.temp-dir=/tmp/code-execution

# Integration
assessment.service.url=http://localhost:8081
question.service.url=http://localhost:8082
```

---

## Error Handling

### Compilation Errors
```json
{
  "status": "COMPILATION_ERROR",
  "error": "Solution.java:5: error: ';' expected\n    int x = 5\n             ^",
  "output": null
}
```

### Runtime Errors
```json
{
  "status": "RUNTIME_ERROR",
  "error": "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException",
  "output": null
}
```

### Timeout
```json
{
  "status": "TIMEOUT",
  "error": "Execution exceeded time limit of 5 seconds",
  "output": null
}
```

---

## Pending Enhancements
- [ ] Code plagiarism detection
- [ ] Advanced code analysis (complexity, best practices)
- [ ] Real-time code execution feedback
- [ ] Code versioning during assessment
- [ ] Support for more languages (C#, Go, Rust)
- [ ] Custom test case input from candidates
- [ ] Code execution replay
- [ ] Performance benchmarking
- [ ] Memory profiling
- [ ] Code coverage analysis
- [ ] Static code analysis
- [ ] Code quality metrics

---

## Logging and Monitoring
- Detailed execution logs
- Evaluation process tracking
- Error logging for debugging
- Performance metrics collection
- See: `EVALUATION_LOGGING_GUIDE.md`

---

## API Documentation
Detailed API documentation available in:
- `API_DOCUMENTATION.md`
- `EVALUATION_API_DOCUMENTATION.md`

---

**Status:** ✅ Production Ready  
**Test Coverage:** Partial (manual testing completed)  
**Code Execution:** Stable and secure

