# Score Display Setup Guide

## Problem
Scores are not visible in "My Tests" after evaluation.

## Solution Steps

### Step 1: Update Database Schema

Run the SQL migration script to add the `max_score` column:

```bash
# Navigate to AssessmentService directory
cd /home/ravindra162/Desktop/prepNow/AssessmentService

# Run the SQL script (you'll need to use sudo for docker)
sudo docker exec -i assessment_cont psql -U postgres -d assessmentdb < add_max_score_column.sql
```

OR run this single command:

```bash
sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "ALTER TABLE assessment_candidates ADD COLUMN IF NOT EXISTS max_score DOUBLE PRECISION;"
```

### Step 2: Verify Database Schema

Check if the column was added successfully:

```bash
sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'assessment_candidates' AND column_name IN ('total_score', 'max_score', 'percentage_score', 'is_passed') ORDER BY column_name;"
```

You should see:
- is_passed
- max_score
- percentage_score
- total_score

### Step 3: Restart Both Services

**In IntelliJ IDEA:**
1. Stop AssessmentService
2. Stop SubmissionService
3. Restart AssessmentService (wait for it to fully start)
4. Restart SubmissionService

### Step 4: Test the Evaluation Flow

1. **Submit a test** through your frontend (if you haven't already)

2. **Get the submission ID** from the response or MongoDB:
   ```bash
   sudo docker exec submissionDB mongosh submissiondb --eval "db.submissions.find().sort({_id:-1}).limit(1).pretty()"
   ```

3. **Call the evaluation API** with the submission ID:
   ```bash
   curl -X POST http://localhost:8083/api/evaluations/submission/YOUR_SUBMISSION_ID \
     -H "Content-Type: application/json" \
     -d '{}'
   ```

4. **Check the logs** - You should see:
   ```
   ✓ Evaluation saved to MongoDB with ID: eval_xxx
   ✓ Submission updated with evaluation results
   ✓ Synced evaluation scores to AssessmentService for candidate ID: 123
   ```

### Step 5: Verify Scores in Database

Check if scores were updated in the database:

```bash
sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "SELECT id, user_ref, status, total_score, max_score, percentage_score, is_passed FROM assessment_candidates WHERE status = 'EVALUATED' ORDER BY id DESC LIMIT 5;"
```

You should see data like:
```
 id | user_ref |  status   | total_score | max_score | percentage_score | is_passed 
----+----------+-----------+-------------+-----------+------------------+-----------
  5 |        1 | EVALUATED |        75.0 |     100.0 |            75.00 | t
```

### Step 6: Verify in Frontend

Go to "My Tests" page and you should now see:
- **Score: 75/100 (75%)**
- **Status: EVALUATED** or **PASSED/FAILED**

---

## What Was Changed

### 1. Database Schema
- Added `max_score` column to `assessment_candidates` table
- This allows storing both user's score (total_score) and maximum possible score (max_score)

### 2. AssessmentService
- **New DTO**: `UpdateScoreRequest` - Contains all score data
- **New Endpoint**: `PUT /assessments/candidates/{candidateId}/score` - Receives scores from SubmissionService
- **New Method**: `updateCandidateScore()` - Updates all score fields in database

### 3. SubmissionService
- **New Method**: `syncScoresToAssessmentService()` - Automatically called after evaluation
- Sends evaluation results to AssessmentService via REST API
- Updates the `assessment_candidates` table with:
  - totalScore (user's score)
  - maxScore (total possible score)
  - percentageScore
  - isPassed (true/false)
  - All statistics (MCQ, Coding, attempted, correct, etc.)

### 4. Flow After Evaluation
```
User submits test
    ↓
POST /api/evaluations/submission/{id}
    ↓
SubmissionService evaluates answers
    ↓
Scores saved to MongoDB (evaluations collection)
    ↓
✨ NEW: Scores synced to AssessmentService
    ↓
PUT /assessments/candidates/{id}/score
    ↓
assessment_candidates table updated
    ↓
Frontend displays: "Score: 75/100 (75%)"
```

---

## Troubleshooting

### If scores still don't show:

1. **Check SubmissionService logs** for:
   ```
   ✓ Synced evaluation scores to AssessmentService for candidate ID: X
   ```
   
2. **If you see an error**, check:
   - Is AssessmentService running on port 8081?
   - Is the candidate ID in metadata?
   
3. **Check the database directly**:
   ```bash
   sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "SELECT * FROM assessment_candidates WHERE id = YOUR_CANDIDATE_ID;"
   ```

4. **Re-evaluate a submission** to trigger the sync:
   ```bash
   # First, delete the existing evaluation
   sudo docker exec submissionDB mongosh submissiondb --eval "db.evaluations.deleteOne({submissionId: 'YOUR_SUBMISSION_ID'})"
   
   # Then re-evaluate
   curl -X POST http://localhost:8083/api/evaluations/submission/YOUR_SUBMISSION_ID -H "Content-Type: application/json" -d '{}'
   ```

---

## Quick Commands Reference

### Add max_score column:
```bash
sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "ALTER TABLE assessment_candidates ADD COLUMN IF NOT EXISTS max_score DOUBLE PRECISION;"
```

### Check scores:
```bash
sudo docker exec assessment_cont psql -U postgres -d assessmentdb -c "SELECT id, user_ref, status, total_score, max_score, percentage_score FROM assessment_candidates ORDER BY id DESC LIMIT 5;"
```

### View submissions in MongoDB:
```bash
sudo docker exec submissionDB mongosh submissiondb --eval "db.submissions.find().pretty()"
```

### View evaluations in MongoDB:
```bash
sudo docker exec submissionDB mongosh submissiondb --eval "db.evaluations.find().pretty()"
```

### Tail SubmissionService logs:
```bash
tail -f logs/SubmissionService.log | grep -E "evaluation|sync|score"
```

---

## Success Criteria

✅ Database has `max_score` column
✅ Both services rebuilt and restarted
✅ Evaluation API returns success
✅ Logs show "✓ Synced evaluation scores to AssessmentService"
✅ Database shows total_score and max_score populated
✅ Frontend displays "Score: X/Y (Z%)"

---

**After following these steps, your "My Tests" page should display scores in the format: "Score: 75/100 (75%)" for all evaluated tests!**

