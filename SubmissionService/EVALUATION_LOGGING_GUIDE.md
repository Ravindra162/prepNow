# Evaluation Logging Documentation

## Overview
The evaluation service now provides comprehensive, detailed logging for every step of the evaluation process. This makes it easy to track exactly how each question was evaluated.

## Log Output Format

When you call the evaluation API, you will see detailed logs like this:

### 1. Evaluation Start
```
========================================
STARTING EVALUATION FOR SUBMISSION: 67234abc123
========================================
Submission Details:
  - User ID: 1234
  - Test ID: 5
  - Created At: 2025-10-24T10:00:00Z
Total answers submitted: 15
Fetching assessment structure for testId: 5
✓ Assessment structure fetched successfully
```

### 2. Question-by-Question Evaluation
```
========================================
EVALUATING QUESTIONS
========================================

--- Section ID: 1 ---

Question #1 (ID: 101)
  Type: MCQ
  Points: 5
  Text: What is the time complexity of binary search?
  → User Selected: A
  → Correct Answer: A
  → Result: ✓ CORRECT (5/5 points)

Question #2 (ID: 102)
  Type: MCQ
  Points: 5
  Text: Which data structure uses LIFO principle?
  → User Selected: B
  → Correct Answer: C
  → Result: ✗ WRONG (0/5 points)

Question #3 (ID: 103)
  Type: MCQ
  Points: 5
  Text: What is the purpose of a hash table?
  → User Selected: NOT ANSWERED
  → Correct Answer: A
  → Result: ⊘ NOT ATTEMPTED (0/5 points)

Question #4 (ID: 104)
  Type: CODING
  Points: 10
  Text: Implement a function to reverse a linked list
  → User Answer: Code submitted
  → Result: ✓ SUBMITTED (10/10 points)

--- Section ID: 2 ---

Question #5 (ID: 201)
  Type: MCQ
  Points: 5
  Text: What is polymorphism?
  → User Selected: C
  → Correct Answer: C
  → Result: ✓ CORRECT (5/5 points)
```

### 3. Evaluation Summary
```
========================================
EVALUATION SUMMARY
========================================
Total Questions: 15
Attempted: 14
Correct: 10
Incorrect: 4
Unanswered: 1

MCQ Performance:
  - Total MCQ: 12
  - Correct: 8
  - Score: 40.0/60.0 points

Coding Performance:
  - Total Coding: 3
  - Passed: 2
  - Score: 20.0/30.0 points

FINAL SCORE: 60.0/90.0 points (66.67%)
Status: ✓ PASSED (Threshold: 60.0%)
========================================
✓ Evaluation saved to MongoDB with ID: eval_67890
✓ Submission updated with evaluation results
```

## Log Symbols Explained

- **✓** - Success/Correct answer
- **✗** - Wrong answer
- **⊘** - Not attempted
- **⚠️** - Warning/Issue detected
- **❌** - Error

## Information Logged for Each Question

### For MCQ Questions:
1. Question number and ID
2. Question type (MCQ)
3. Points available
4. Question text (truncated if too long)
5. **User's selected option** (e.g., "A", "B", "C", "D")
6. **Correct option** (e.g., "A", "B", "C", "D")
7. **Result** - One of:
   - `✓ CORRECT (X/X points)` - User answered correctly
   - `✗ WRONG (0/X points)` - User answered incorrectly
   - `⊘ NOT ATTEMPTED (0/X points)` - User didn't answer

### For Coding Questions:
1. Question number and ID
2. Question type (CODING)
3. Points available
4. Question text (truncated if too long)
5. **User's submission status** (Code submitted or No answer)
6. **Result** - One of:
   - `✓ SUBMITTED (X/X points)` - User submitted code
   - `✗ NOT SUBMITTED (0/X points)` - User didn't submit code

## Where to View Logs

### Option 1: IntelliJ IDEA Console
When running SubmissionService in IntelliJ IDEA, all logs appear in the console/run window.

### Option 2: Log Files
Check the log file:
```bash
tail -f /home/ravindra162/Desktop/prepNow/logs/SubmissionService.log
```

### Option 3: Docker Logs (if running in Docker)
```bash
docker logs -f submission-service
```

## Example Complete Log Flow

```
2025-10-24 15:45:00.123 INFO  --- [nio-8083-exec-1] c.S.S.c.EvaluationController : POST /api/evaluations/submission/67234abc123 - Starting evaluation
2025-10-24 15:45:00.125 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.125 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : STARTING EVALUATION FOR SUBMISSION: 67234abc123
2025-10-24 15:45:00.125 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.130 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Submission Details:
2025-10-24 15:45:00.130 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - User ID: 1234
2025-10-24 15:45:00.130 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Test ID: 5
2025-10-24 15:45:00.130 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Created At: 2025-10-24T10:15:00Z
2025-10-24 15:45:00.135 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Total answers submitted: 12
2025-10-24 15:45:00.135 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Fetching assessment structure for testId: 5
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ✓ Assessment structure fetched successfully
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : EVALUATING QUESTIONS
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.256 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : --- Section ID: 1 ---
2025-10-24 15:45:00.257 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.257 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Question #1 (ID: 101)
2025-10-24 15:45:00.257 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   Type: MCQ
2025-10-24 15:45:00.257 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   Points: 5
2025-10-24 15:45:00.257 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   Text: What is the time complexity of binary search?
2025-10-24 15:45:00.258 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   → User Selected: A
2025-10-24 15:45:00.258 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   → Correct Answer: A
2025-10-24 15:45:00.258 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   → Result: ✓ CORRECT (5/5 points)
... (continues for each question)
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : EVALUATION SUMMARY
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Total Questions: 12
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Attempted: 11
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Correct: 8
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Incorrect: 3
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Unanswered: 1
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : MCQ Performance:
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Total MCQ: 10
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Correct: 7
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Score: 35.0/50.0 points
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Coding Performance:
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Total Coding: 2
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Passed: 1
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    :   - Score: 10.0/20.0 points
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : 
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : FINAL SCORE: 45.0/70.0 points (64.29%)
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : Status: ✓ PASSED (Threshold: 60.0%)
2025-10-24 15:45:00.456 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ========================================
2025-10-24 15:45:00.567 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ✓ Evaluation saved to MongoDB with ID: eval_67890
2025-10-24 15:45:00.578 INFO  --- [nio-8083-exec-1] c.S.S.s.EvaluationService    : ✓ Submission updated with evaluation results
```

## How to Use

1. **Start all services** (AssessmentService, QuestionService, SubmissionService)
2. **Submit a test** through the frontend
3. **Call the evaluation API**:
   ```bash
   curl -X POST http://localhost:8083/api/evaluations/submission/YOUR_SUBMISSION_ID \
     -H "Content-Type: application/json" \
     -d '{}'
   ```
4. **Check logs** in IntelliJ console or log file to see detailed evaluation process

## Benefits of Detailed Logging

1. **Transparency** - See exactly how each answer was evaluated
2. **Debugging** - Quickly identify issues with questions or answers
3. **Audit Trail** - Complete record of evaluation process
4. **Performance Analysis** - Track which questions students struggle with
5. **Verification** - Confirm evaluation logic is working correctly

## Filtering Logs

To see only evaluation logs:
```bash
grep "EVALUATION\|Question #\|User Selected\|Correct Answer\|Result:" logs/SubmissionService.log
```

To see only the summary:
```bash
grep -A 20 "EVALUATION SUMMARY" logs/SubmissionService.log
```

---

**The enhanced logging is now ready!** When you restart the SubmissionService and call the evaluation API, you'll see comprehensive, question-by-question evaluation details in your logs.

