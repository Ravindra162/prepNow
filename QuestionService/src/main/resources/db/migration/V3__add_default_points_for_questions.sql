-- Add default value for points column in questions table
-- Default: 50 points for coding questions, 1 point for MCQ questions
-- This is a safety measure, the application logic handles this, but good to have DB default too

ALTER TABLE questions ALTER COLUMN points SET DEFAULT 1;

-- Update any existing questions that have NULL points
UPDATE questions SET points = 50 WHERE points IS NULL AND type = 'CODING';
UPDATE questions SET points = 1 WHERE points IS NULL AND type = 'MCQ';

-- Optional: Add a comment to document the default behavior
COMMENT ON COLUMN questions.points IS 'Points awarded for this question. Default: 50 for CODING, 1 for MCQ';

