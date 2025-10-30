# Submission Evaluation API Documentation

## Overview
The Submission Evaluation API automatically evaluates user test submissions by comparing their answers with correct answers from the QuestionService and AssessmentService.

## Implementation Summary

### ✅ What Was Implemented

1. **Enhanced Evaluation Domain Model**
   - Added detailed question-level results
   - Added MCQ vs Coding score breakdown
   - Added statistics (attempted, correct, incorrect, unanswered)
   - Added passing threshold and pass/fail status

2. **Created DTOs**
   - `EvaluateSubmissionRequest` - Request parameters for evaluation
   - `EvaluationResponse` - Comprehensive evaluation results
   - `QuestionResult` - Individual question evaluation details

3. **Implemented Evaluation Service**
   - Fetches submission data from MongoDB
   - Fetches assessment structure from AssessmentService
   - Fetches questions with correct answers from QuestionService
   - Automatically evaluates MCQ questions by comparing answers
   - Calculates detailed scores and statistics
   - Saves evaluation results to MongoDB

4. **Updated Evaluation Controller**
   - `POST /api/evaluations/submission/{submissionId}` - Evaluate a submission
   - `GET /api/evaluations/{evaluationId}` - Get evaluation by ID
   - `GET /api/evaluations/submission/{submissionId}` - Get evaluation by submission ID

5. **Added EVALUATED Status**
   - New status in `SubmissionStatus` enum to track evaluated submissions

---

## API Endpoints

### 1. Evaluate Submission
**Endpoint:** `POST /api/evaluations/submission/{submissionId}`

**Description:** Evaluates a submission by comparing user answers with correct answers.

**Request Body (Optional):**
```json
{
  "passingThreshold": 60.0,
  "autoEvaluateCoding": true
}
```

**Parameters:**
- `passingThreshold` (optional): Percentage required to pass (default: 60.0)
- `autoEvaluateCoding` (optional): Whether to auto-evaluate coding questions (not yet implemented)

**Example cURL:**
```bash
curl -X POST http://localhost:8083/api/evaluations/submission/{submissionId} \
  -H "Content-Type: application/json" \
  -d '{"passingThreshold": 60.0}'
```

**Response:**
```json
{
  "id": "eval_123",
  "submissionId": "sub_456",
  "totalScore": 75.0,
  "maxScore": 100.0,
  "percentageScore": 75.0,
  "mcqScore": 45.0,
  "mcqMaxScore": 60.0,
  "mcqCorrect": 9,
  "mcqTotal": 12,
  "codingScore": 30.0,
  "codingMaxScore": 40.0,
  "codingPassed": 3,
  "codingTotal": 4,
  "evaluatedAt": "2025-10-24T10:00:00Z",
  "passed": true,
  "passingThreshold": 60.0,
  "totalQuestionsAttempted": 15,
  "totalQuestionsCorrect": 12,
  "totalQuestionsIncorrect": 3,
  "totalQuestionsUnanswered": 1,
  "questionResults": [
    {
      "questionId": "1",
      "questionType": "MCQ",
      "userAnswer": "A",
      "correctAnswer": "A",
      "isCorrect": true,
      "pointsAwarded": 5.0,
      "maxPoints": 5.0,
      "feedback": "Correct!",
      "difficulty": "EASY"
    },
    {
      "questionId": "2",
      "questionType": "MCQ",
      "userAnswer": "B",
      "correctAnswer": "C",
      "isCorrect": false,
      "pointsAwarded": 0.0,
      "maxPoints": 5.0,
      "feedback": "Incorrect. Correct answer: C",
      "difficulty": "MEDIUM"
    }
  ]
}
```

---

### 2. Get Evaluation by ID
**Endpoint:** `GET /api/evaluations/{evaluationId}`

**Description:** Retrieves evaluation details by evaluation ID.

**Example:**
```bash
curl http://localhost:8083/api/evaluations/{evaluationId}
```

---

### 3. Get Evaluation by Submission ID
**Endpoint:** `GET /api/evaluations/submission/{submissionId}`

**Description:** Retrieves evaluation details for a specific submission.

**Example:**
```bash
curl http://localhost:8083/api/evaluations/submission/{submissionId}
```

---

## How It Works

### Evaluation Flow:

1. **Fetch Submission Data**
   - Retrieves submission from MongoDB
   - Extracts user answers from metadata
   - Gets test/assessment ID

2. **Fetch Assessment Structure**
   - Calls AssessmentService: `GET /assessments/{testId}/structure`
   - Gets all questions with their details and MCQ options

3. **Evaluate Each Question**
   - **MCQ Questions:**
     - Compares user's selected option with correct option
     - Awards full points if correct, 0 if incorrect
   - **Coding Questions:**
     - Currently marks as correct if code was submitted
     - Future: Will run test cases for actual evaluation

4. **Calculate Scores**
   - Total score and max score
   - MCQ breakdown (score, correct count, total)
   - Coding breakdown (score, passed count, total)
   - Percentage score
   - Pass/fail status based on threshold

5. **Save Results**
   - Saves detailed evaluation to MongoDB
   - Updates submission status to EVALUATED
   - Links evaluation to submission

---

## Data Models

### Evaluation
```javascript
{
  id: String,
  submissionId: String (unique),
  totalScore: Double,
  maxScore: Double,
  percentageScore: Double,
  
  // MCQ Statistics
  mcqScore: Double,
  mcqMaxScore: Double,
  mcqCorrect: Integer,
  mcqTotal: Integer,
  
  // Coding Statistics  
  codingScore: Double,
  codingMaxScore: Double,
  codingPassed: Integer,
  codingTotal: Integer,
  
  // Question-level results
  questionResults: [QuestionResult],
  
  // Overall statistics
  totalQuestionsAttempted: Integer,
  totalQuestionsCorrect: Integer,
  totalQuestionsIncorrect: Integer,
  totalQuestionsUnanswered: Integer,
  
  // Pass/Fail
  passed: Boolean,
  passingThreshold: Double,
  
  evaluatedAt: Instant
}
```

### QuestionResult
```javascript
{
  questionId: String,
  questionType: String, // "MCQ" or "CODING"
  userAnswer: String,
  correctAnswer: String,
  isCorrect: Boolean,
  pointsAwarded: Double,
  maxPoints: Double,
  feedback: String,
  difficulty: String
}
```

---

## Usage Example

### Complete Workflow:

1. **User submits assessment**
   ```bash
   # Submission is created automatically when user submits test
   # via AssessmentService -> SubmissionService integration
   ```

2. **Evaluate the submission**
   ```bash
   curl -X POST http://localhost:8083/api/evaluations/submission/67234abc123 \
     -H "Content-Type: application/json" \
     -d '{"passingThreshold": 70.0}'
   ```

3. **Get evaluation results**
   ```bash
   curl http://localhost:8083/api/evaluations/submission/67234abc123
   ```

---

## Database Collections

### MongoDB Collections Updated:

1. **submissions** - Updated with:
   - `totalScore`
   - `maxScore`
   - `evaluationId`
   - `status` = "EVALUATED"

2. **evaluations** - New documents with full evaluation details

---

## Testing the API

### Prerequisites:
1. MongoDB running on port 27017
2. AssessmentService running on port 8081
3. QuestionService running on port 8082
4. SubmissionService running on port 8083

### Test Scenario:

1. **Submit an assessment** (via frontend or API)
2. **Note the submission ID** from the response
3. **Evaluate it:**
   ```bash
   curl -X POST http://localhost:8083/api/evaluations/submission/YOUR_SUBMISSION_ID \
     -H "Content-Type: application/json" \
     -d '{}'
   ```
4. **View results:**
   ```bash
   curl http://localhost:8083/api/evaluations/submission/YOUR_SUBMISSION_ID
   ```

---

## Features Implemented ✅

- ✅ Automatic MCQ evaluation (compares with correct answers)
- ✅ Score calculation (total, MCQ, coding breakdown)
- ✅ Question-level results with feedback
- ✅ Pass/fail determination with configurable threshold
- ✅ Statistics tracking (attempted, correct, incorrect, unanswered)
- ✅ Detailed feedback for each question
- ✅ Integration with AssessmentService and QuestionService
- ✅ Prevents duplicate evaluations
- ✅ Updates submission status after evaluation

## Future Enhancements 🚀

- ⏳ Actual coding question evaluation (run test cases)
- ⏳ Difficulty-level score breakdown
- ⏳ Section-wise score breakdown
- ⏳ Time-based scoring
- ⏳ Negative marking for incorrect answers
- ⏳ Partial credit for coding questions
- ⏳ AI-based evaluation for subjective questions

---

## Error Handling

The API handles the following scenarios:

- **Submission not found** - Returns 404 with error message
- **No answers found** - Returns 400 with error message
- **Assessment structure not available** - Returns 500 with error message
- **Already evaluated** - Returns existing evaluation (idempotent)
- **Service unavailable** - Returns 503 if dependent services are down

---

## MongoDB Verification

To verify data in MongoDB:

```bash
# Connect to MongoDB
sudo docker exec -it submissionDB mongosh

# Switch to database
use submissiondb

# View submissions
db.submissions.find().pretty()

# View evaluations
db.evaluations.find().pretty()

# View a specific evaluation
db.evaluations.findOne({submissionId: "YOUR_SUBMISSION_ID"})
```

---

## Integration Points

### Services Communication:

```
SubmissionService (Port 8083)
    ↓
    ├─→ AssessmentService (Port 8081)
    │   └─→ GET /assessments/{testId}/structure
    │       (Returns questions with MCQ options)
    │
    └─→ QuestionService (Port 8082)
        └─→ GET /questions/{questionId}
            (Returns question details if needed)
```

---

## Status Codes

- `201 Created` - Evaluation successfully created
- `200 OK` - Evaluation retrieved successfully
- `400 Bad Request` - Invalid request or no answers found
- `404 Not Found` - Submission or evaluation not found
- `500 Internal Server Error` - Server error or service unavailable

---

## Summary

The evaluation system is now **fully functional** and will:
1. ✅ Automatically evaluate MCQ questions
2. ✅ Calculate detailed scores and statistics
3. ✅ Provide question-level feedback
4. ✅ Determine pass/fail status
5. ✅ Store comprehensive results in MongoDB
6. ✅ Update submission status appropriately

**You can now call the evaluation API for any submission ID to get instant, detailed evaluation results!**

