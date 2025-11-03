# Coding Question Scoring Implementation

## Summary

This document describes the implementation of the scoring system for coding questions based on test case pass rates.

## Scoring Formula

For coding questions, the score is calculated as:

```
Score = (Number of Passed Test Cases / Total Number of Test Cases) × Maximum Points
```

## Current Implementation Status

### ✅ Question Creation - Score Field with Default Value

The `Question` entity has a `points` field that can be set during question creation:

- **Location**: `QuestionService/src/main/java/com/Question/Questions/entity/Question.java`
- **Field**: `private Integer points;` (Line 42)
- **DTO Support**: `QuestionCreateRequest` includes `points` field for API input
- **Database**: Stored in PostgreSQL `questions` table with column `points`
- **Default Value**: 
  - **50 points** for CODING questions (if not specified)
  - **1 point** for MCQ questions (if not specified)
- **Customizable**: You can override the default by providing a custom `points` value

### Example 1: Creating a Coding Question **WITHOUT** Custom Points (uses default 50)

```json
{
  "questionText": "Write a function to reverse a string",
  "questionType": "CODING",
  "difficultyLevel": "MEDIUM",
  "timeLimitMinutes": 30,
  "programmingLanguage": "python",
  "codeTemplate": "def reverse_string(s):\n    # Your code here\n    pass",
  "testCases": [
    {
      "input": "hello",
      "expectedOutput": "olleh",
      "isHidden": false
    },
    {
      "input": "world",
      "expectedOutput": "dlrow",
      "isHidden": true
    }
  ]
}
```
**Result**: This question will automatically be assigned **50 points**.

### Example 2: Creating a Coding Question **WITH** Custom Points

```json
{
  "questionText": "Implement a binary search algorithm",
  "questionType": "CODING",
  "difficultyLevel": "HARD",
  "points": 100,
  "timeLimitMinutes": 45,
  "programmingLanguage": "java",
  "codeTemplate": "public int binarySearch(int[] arr, int target) {\n    // Your code here\n    return -1;\n}",
  "testCases": [
    {
      "input": "[1,2,3,4,5]\n3",
      "expectedOutput": "2",
      "isHidden": false
    },
    {
      "input": "[1,2,3,4,5]\n6",
      "expectedOutput": "-1",
      "isHidden": true
    }
  ]
}
```
**Result**: This question will be assigned **100 points** (custom value).

### Example 3: Creating an MCQ Question (default 1 point)

```json
{
  "questionText": "What is the time complexity of binary search?",
  "questionType": "MCQ",
  "difficultyLevel": "EASY",
  "mcqOptions": [
    {"optionText": "O(n)", "isCorrect": false},
    {"optionText": "O(log n)", "isCorrect": true},
    {"optionText": "O(n^2)", "isCorrect": false},
    {"optionText": "O(1)", "isCorrect": false}
  ]
}
```
**Result**: This question will automatically be assigned **1 point**.

### ✅ Evaluation Service - Updated for Test Case-Based Scoring

**Modified Files:**

1. **QuestionResult.java** - Added test case tracking fields:
   - `totalTestCases` - Total number of test cases for the question
   - `passedTestCases` - Number of test cases the user's code passed
   - `testCaseResults` - Detailed results for each test case

2. **EvaluationService.java** - Implemented proper coding evaluation:
   - `evaluateCodingQuestion()` - New method that:
     - Checks if code was submitted
     - Fetches test cases from question data
     - Runs each test case against user's code
     - Calculates score based on pass rate: `(passed/total) × maxPoints`
     - Returns detailed results including which test cases passed/failed
   
   - `fetchAndRunTestCases()` - Executes test cases:
     - Runs user code with each test case input
     - Compares actual output with expected output
     - Tracks pass/fail status for each test case
     - Respects `isSample` flag to hide non-sample test case details
   
   - `compareOutputs()` - Normalizes and compares outputs:
     - Trims whitespace
     - Normalizes line endings
     - Performs exact string comparison

## Evaluation Logic

### For Coding Questions:

1. **No Code Submitted**: 0 points
   ```
   Score = 0
   Status: Not Submitted
   ```

2. **Code Submitted, No Test Cases**: Full points awarded
   ```
   Score = maxPoints
   Status: Code Submitted (no validation)
   ```

3. **Code Submitted with Test Cases**: Proportional scoring
   ```
   Score = (passedTestCases / totalTestCases) × maxPoints
   Status: Partial Success or All Tests Passed
   ```

### Examples:

**Example 1: Partial Success**
- Max Points: 10
- Total Test Cases: 5
- Passed Test Cases: 3
- **Score Awarded**: (3/5) × 10 = **6.0 points**

**Example 2: All Tests Passed**
- Max Points: 10
- Total Test Cases: 5
- Passed Test Cases: 5
- **Score Awarded**: (5/5) × 10 = **10.0 points**

**Example 3: All Tests Failed**
- Max Points: 10
- Total Test Cases: 5
- Passed Test Cases: 0
- **Score Awarded**: (0/5) × 10 = **0.0 points**

## Evaluation Response Structure

The evaluation response now includes detailed test case information:

```json
{
  "questionResults": [
    {
      "questionId": "123",
      "questionType": "CODING",
      "userAnswer": "def reverse_string(s):\n    return s[::-1]",
      "isCorrect": true,
      "pointsAwarded": 10.0,
      "maxPoints": 10.0,
      "totalTestCases": 5,
      "passedTestCases": 5,
      "feedback": "Passed 5 out of 5 test cases",
      "testCaseResults": [
        {
          "testCaseNumber": 1,
          "input": "hello",
          "expectedOutput": "olleh",
          "actualOutput": "olleh",
          "passed": true,
          "isSample": true
        },
        {
          "testCaseNumber": 2,
          "input": "Hidden",
          "expectedOutput": "Hidden",
          "actualOutput": "...",
          "passed": true,
          "isSample": false
        }
      ]
    }
  ],
  "codingScore": 10.0,
  "codingMaxScore": 10.0,
  "codingPassed": 1,
  "codingTotal": 1
}
```

## Test Case Visibility

- **Sample Test Cases** (`isSample: true`): Input and expected output are visible in evaluation results
- **Hidden Test Cases** (`isSample: false`): Input and expected output are masked as "Hidden"

## Database Schema

### Question Entity Fields:
- `question_id` (Primary Key)
- `question_text` (TEXT)
- `type` (ENUM: MCQ, CODING)
- `points` (INTEGER) - **Score for the question**
- `difficulty_level` (ENUM)
- `programming_language` (VARCHAR)
- `code_template` (TEXT)

### TestCase Entity Fields:
- `test_case_id` (Primary Key)
- `question_id` (Foreign Key)
- `input_data` (TEXT)
- `expected_output` (TEXT)
- `is_sample` (BOOLEAN) - Determines visibility
- `test_case_order` (INTEGER)

## Integration Points

### 1. Question Service
- **Endpoint**: `POST /questions`
- **Accepts**: `points` field in request body
- **Stores**: Points value in database

### 2. Assessment Service
- **Endpoint**: `GET /assessments/{testId}/structure`
- **Returns**: Questions with points and test cases

### 3. Submission Service
- **Endpoint**: `POST /evaluations/{submissionId}`
- **Process**:
  1. Fetches assessment structure
  2. Retrieves user answers
  3. For each coding question:
     - Runs test cases
     - Calculates proportional score
     - Updates evaluation results

## TODO: Code Execution Integration

**Current Status**: The evaluation currently uses simulated test case execution.

**Next Steps**: Integrate with PistonApiService for real code execution:

```java
private String executeCodeWithInput(String code, String input, String language) {
    // TODO: Implement actual code execution
    RunCodeRequest request = new RunCodeRequest();
    request.setCode(code);
    request.setLanguage(language);
    request.setStdin(input);
    request.setRunTimeout(3000);
    
    RunCodeResponse response = pistonApiService.executeCode(request);
    return response.getRun().getStdout();
}
```

**Required Changes**:
1. Inject `PistonApiService` into `EvaluationService`
2. Replace `executeCodeWithInput()` simulation with actual API calls
3. Handle execution errors, timeouts, and memory limits
4. Add retry logic for rate limiting (Piston API: 1 request per 250ms)

## Testing the Implementation

### 1. Create a Coding Question
```bash
curl -X POST http://localhost:8082/questions \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "Sum two numbers",
    "questionType": "CODING",
    "points": 10,
    "programmingLanguage": "python",
    "testCases": [
      {"input": "2\n3", "expectedOutput": "5", "isHidden": false},
      {"input": "10\n20", "expectedOutput": "30", "isHidden": true}
    ]
  }'
```

### 2. Submit Code
```bash
curl -X POST http://localhost:8083/submissions \
  -H "Content-Type: application/json" \
  -d '{
    "testId": "1",
    "userId": "user123",
    "answers": {
      "123": "a = int(input())\nb = int(input())\nprint(a + b)"
    }
  }'
```

### 3. Evaluate Submission
```bash
curl -X POST http://localhost:8083/evaluations/{submissionId}
```

## Benefits

1. **Fair Scoring**: Partial credit for partially correct solutions
2. **Detailed Feedback**: Students see which test cases passed/failed
3. **Flexible**: Works with any number of test cases
4. **Transparent**: Clear scoring formula based on test case pass rate
5. **Extensible**: Easy to add more test cases to existing questions

## Notes

- A coding question is marked as "correct" (`isCorrect: true`) only if ALL test cases pass
- However, partial points are still awarded for partial success
- The `codingPassed` counter only increments when ALL test cases pass
- This allows distinguishing between "fully correct" and "partially correct" solutions
