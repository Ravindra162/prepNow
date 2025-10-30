# Test Submission Feature Implementation

## Overview
Implemented a comprehensive test submission system that allows users to:
1. Submit their assessment answers
2. View their attempted tests with submission details
3. Track test status (Pending/Completed/Evaluated)

## Backend Changes

### 1. Database Schema
The `assessment_candidates` table already exists with all necessary fields:
- `answers` (JSONB) - Stores user's answers
- `status` (ENUM) - INVITED, IN_PROGRESS, COMPLETED, EVALUATED
- `started_at`, `completed_at` - Timestamps
- `time_taken_minutes`, `time_remaining_minutes` - Duration tracking
- `submission_method` (ENUM) - MANUAL_SUBMIT, AUTO_SUBMIT, TIME_EXPIRED
- `total_score`, `percentage_score`, `is_passed` - Score fields (for future evaluation)

### 2. New Backend Endpoints

#### Submit Assessment
```
POST /assessments/{assessmentId}/submit?userRef={userRef}
Body: {
  "answers": { "questionId": "answer" },
  "submissionMethod": "MANUAL_SUBMIT",
  "browserInfo": "user-agent-string",
  "ipAddress": null
}
```

#### Get User's Attempted Tests
```
GET /assessments/user/{userRef}/attempts
Returns: List of AssessmentCandidate objects with assessment details
```

### 3. Service Layer Changes
- Added `getUserAttempts()` method in `AssessmentAttemptService`
- Added repository method `findByUserRefOrderByCreatedAtDesc()` in `AssessmentCandidateRepository`
- Submission stores answers, updates status, calculates time taken

## Frontend Changes

### 1. New Components

#### MyTests Page (`/my-tests`)
- Displays all attempted assessments for the logged-in user
- Shows test status with color-coded badges
- Displays submission details (started, completed, time taken)
- Shows score status (Pending/Evaluated)
- Empty state when no tests attempted

### 2. Updated Components

#### AssessmentTest Page
- Added submission confirmation dialog
- Integrated with backend API to submit answers
- Auto-submit on timer expiration
- Redirects to `/my-tests` after successful submission
- Stores submission method (manual/time-expired)

#### Navigation
- Added "My Tests" link in Companies page navigation
- Easy access to view attempted tests

### 3. API Integration (`adminApi.js`)
Added new methods:
- `submitAssessment(assessmentId, userRef, submissionData)`
- `getUserAttemptedAssessments(userRef)`
- `startAssessmentAttempt(assessmentId, userRef)`
- `updateProgress(assessmentId, userRef, progressData)`
- `getAssessmentResults(assessmentId, userRef)`

### 4. Routing
Added route: `/my-tests` (Protected route requiring authentication)

## User Flow

1. **Start Test**: User navigates to assessment instructions and starts test
2. **Answer Questions**: User answers questions, timer counts down
3. **Submit Test**: 
   - User clicks "Submit" button (manual submission)
   - Or timer expires (auto-submission)
   - Confirmation dialog shown
4. **View Results**: User redirected to "My Tests" page
5. **Track Progress**: User can view all attempted tests, see pending scores

## User Reference System
Uses a hash of the user's email to generate a numeric `userRef`:
- Consistent across sessions
- No need for database user ID
- Simple and works with current auth system

## Future Enhancements (Pending)
- Automatic answer evaluation for MCQ questions
- Score calculation based on correct answers
- Detailed analytics per section
- Ability to view answers after submission
- Integration with evaluation service for coding questions
- Admin panel to view all user submissions

## Testing
To test the implementation:

1. Start all services:
   - AuthService (port 8080)
   - AssessmentService (port 8081)
   - QuestionService (port 8082)
   - Frontend (port 5173)

2. Login as a user
3. Navigate to Companies page
4. Select an assessment and start test
5. Answer some questions
6. Click Submit
7. View submitted test in "My Tests" page

## Database Verification
Check submissions in database:
```sql
SELECT * FROM assessment_candidates WHERE status = 'COMPLETED';
```

