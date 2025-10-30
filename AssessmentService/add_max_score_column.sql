-- SQL Migration Script to add max_score column
-- Run this script to update the assessment_candidates table

-- Add max_score column if it doesn't exist
ALTER TABLE assessment_candidates ADD COLUMN IF NOT EXISTS max_score DOUBLE PRECISION;

-- Verify the columns
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'assessment_candidates'
  AND column_name IN ('total_score', 'max_score', 'percentage_score', 'is_passed')
ORDER BY column_name;

-- Check current data
SELECT id, user_ref, status, total_score, max_score, percentage_score, is_passed
FROM assessment_candidates
ORDER BY id DESC
LIMIT 10;

