# Submission Service API Documentation

## Overview
The Submission Service handles code submissions, execution, and evaluation for tests containing both MCQ and coding questions.

**Base URL:** `http://localhost:8083/api`

---

## Collections (MongoDB Schema)

### 1. submissions
- Stores parent submission records
- Links to files and evaluations
- Tracks overall status and scores

### 2. submission_files
- Stores uploaded code files
- References submission and question
- Contains code content and metadata

### 3. code_executions
- Stores individual code execution results
- Contains test case results, runtime metrics
- Links to submission and file

### 4. evaluations
- Stores final evaluation/scoring
- Aggregates MCQ + coding scores
- Contains question-level breakdown

---

## API Endpoints

### Submission Management

#### Create Submission
```
POST /api/submissions
Content-Type: application/json

{
  "userId": "user123",
  "testId": "test456",
  "metadata": {}
}

Response: 201 Created
{
  "id": "sub_id",
  "userId": "user123",
  "testId": "test456",
  "createdAt": "2025-10-16T10:00:00Z",
  "status": "SUBMITTED",
  "fileIds": [],
  ...
}
```

#### Get Submission
```
GET /api/submissions/{id}

Response: 200 OK
```

#### Get Submissions by User
```
GET /api/submissions/user/{userId}

Response: 200 OK - Array of submissions
```

#### Get Submissions by Test
```
GET /api/submissions/test/{testId}

Response: 200 OK - Array of submissions
```

#### Get Submissions by User and Test
```
GET /api/submissions/user/{userId}/test/{testId}

Response: 200 OK - Array of submissions
```

#### Update Submission Status
```
PATCH /api/submissions/{id}/status

{
  "status": "RUNNING" | "COMPLETED" | "FAILED" | "PARTIAL"
}

Response: 200 OK
```

#### Update Submission Score
```
PATCH /api/submissions/{id}/score

{
  "totalScore": 85.5,
  "maxScore": 100.0
}

Response: 200 OK
```

#### Delete Submission
```
DELETE /api/submissions/{id}

Response: 204 No Content
```

---

### File Management

#### Upload Code
```
POST /api/files
Content-Type: application/json

{
  "submissionId": "sub_id",
  "questionId": "q123",
  "language": "java",
  "content": "public class Solution { ... }",
  "filename": "Solution.java"
}

Response: 201 Created
{
  "id": "file_id",
  "submissionId": "sub_id",
  "questionId": "q123",
  "language": "java",
  "filename": "Solution.java",
  "sizeBytes": 1024,
  "uploadedAt": "2025-10-16T10:05:00Z",
  ...
}
```

#### Get File
```
GET /api/files/{id}

Response: 200 OK - File details with content
```

#### Get Files by Submission
```
GET /api/files/submission/{submissionId}

Response: 200 OK - Array of files
```

#### Get Files by Question
```
GET /api/files/question/{questionId}

Response: 200 OK - Array of files
```

#### Delete File
```
DELETE /api/files/{id}

Response: 204 No Content
```

---

### Code Execution

#### Execute Code
```
POST /api/executions
Content-Type: application/json

{
  "submissionId": "sub_id",
  "fileId": "file_id",
  "questionId": "q123",
  "testCases": [
    {
      "id": "tc1",
      "input": "5",
      "expectedOutput": "120"
    }
  ],
  "timeoutMs": 5000,
  "memoryLimitMb": 256
}

Response: 201 Created
{
  "id": "exec_id",
  "submissionId": "sub_id",
  "fileId": "file_id",
  "questionId": "q123",
  "language": "java",
  "executedAt": "2025-10-16T10:10:00Z",
  "runTimeMs": 150,
  "memoryUsedMb": 45,
  "status": "SUCCESS",
  "testCaseResults": [
    {
      "testCaseId": "tc1",
      "passed": true,
      "expected": "120",
      "actual": "120",
      "timeMs": 50
    }
  ],
  "passedCount": 1,
  "totalCount": 1,
  "score": 100.0,
  ...
}
```

#### Get Execution
```
GET /api/executions/{id}

Response: 200 OK
```

#### Get Executions by Submission
```
GET /api/executions/submission/{submissionId}

Response: 200 OK - Array of executions
```

#### Get Executions by Question
```
GET /api/executions/question/{questionId}

Response: 200 OK - Array of executions
```

---

### Evaluation

#### Evaluate Submission
```
POST /api/evaluations/submission/{submissionId}
Content-Type: application/json

{
  "mcqScore": 30.0,
  "maxMcqScore": 50.0
}

Response: 201 Created
{
  "id": "eval_id",
  "submissionId": "sub_id",
  "totalScore": 130.0,
  "maxScore": 150.0,
  "mcqScore": 30.0,
  "codingScore": 100.0,
  "evaluatedAt": "2025-10-16T10:15:00Z",
  "breakdown": {
    "mcq_section": 30.0,
    "q123": 100.0
  },
  "passed": true,
  "passingThreshold": 60.0,
  ...
}
```

#### Get Evaluation
```
GET /api/evaluations/{id}

Response: 200 OK
```

#### Get Evaluation by Submission
```
GET /api/evaluations/submission/{submissionId}

Response: 200 OK
```

---

## Workflow Example

### Complete Submission Flow

1. **Create Submission**
```
POST /api/submissions
{ "userId": "u1", "testId": "t1" }
→ Returns submissionId
```

2. **Upload Code for Each Question**
```
POST /api/files
{
  "submissionId": "sub_id",
  "questionId": "q1",
  "language": "java",
  "content": "code here"
}
→ Returns fileId
```

3. **Execute Code**
```
POST /api/executions
{
  "submissionId": "sub_id",
  "fileId": "file_id",
  "questionId": "q1",
  "testCases": [...]
}
→ Returns execution results with scores
```

4. **Evaluate Complete Submission**
```
POST /api/evaluations/submission/{submissionId}
{
  "mcqScore": 40,
  "maxMcqScore": 50
}
→ Aggregates all scores and marks submission as COMPLETED
```

5. **Retrieve Final Results**
```
GET /api/submissions/{submissionId}
GET /api/evaluations/submission/{submissionId}
```

---

## Status Enums

### SubmissionStatus
- `SUBMITTED` - Initial state
- `RUNNING` - Code execution in progress
- `COMPLETED` - All evaluations done
- `FAILED` - Execution failed
- `PARTIAL` - Partially completed

### ExecutionStatus
- `PENDING` - Queued for execution
- `RUNNING` - Currently executing
- `SUCCESS` - All test cases passed
- `FAILURE` - Some test cases failed
- `TIMEOUT` - Execution timed out
- `ERROR` - Runtime error
- `COMPILATION_ERROR` - Code didn't compile

---

## Error Responses

```json
{
  "timestamp": "2025-10-16T10:00:00Z",
  "message": "Error description",
  "status": 400
}
```

Common Status Codes:
- 200: Success
- 201: Created
- 204: No Content (successful deletion)
- 400: Bad Request (validation error)
- 404: Not Found
- 500: Internal Server Error

