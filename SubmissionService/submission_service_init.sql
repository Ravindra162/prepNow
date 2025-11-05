-- Submission Service Database Initialization Script
-- This script will create the schema, tables, indexes, and sample data

-- Drop existing schema and all objects (WARNING: This will delete all data!)
DROP SCHEMA IF EXISTS submission_schema CASCADE;

-- Create schema for Submission Service
CREATE SCHEMA submission_schema;

-- Set search path to the new schema
SET search_path TO submission_schema;

-- Create submissions table
CREATE TABLE submissions (
    submission_id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    time_taken_seconds INTEGER,
    score DOUBLE PRECISION,
    max_score DOUBLE PRECISION,
    percentage_score DOUBLE PRECISION,
    is_passed BOOLEAN,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create submission_answers table
CREATE TABLE submission_answers (
    answer_id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_type VARCHAR(50) NOT NULL, -- MCQ or CODING
    user_answer TEXT,
    is_correct BOOLEAN,
    points_awarded DOUBLE PRECISION,
    max_points DOUBLE PRECISION,
    time_taken_seconds INTEGER,
    code_execution_result JSONB,
    feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE
);

-- Create code_executions table
CREATE TABLE code_executions (
    execution_id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    answer_id BIGINT,
    language VARCHAR(50) NOT NULL,
    source_code TEXT NOT NULL,
    stdin TEXT,
    stdout TEXT,
    stderr TEXT,
    exit_code INTEGER,
    time_taken_ms INTEGER,
    memory_used_kb INTEGER,
    status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (answer_id) REFERENCES submission_answers(answer_id) ON DELETE SET NULL
);

-- Create test_case_results table
CREATE TABLE test_case_results (
    test_case_result_id BIGSERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    test_case_id BIGINT,
    input_data TEXT,
    expected_output TEXT,
    actual_output TEXT,
    is_passed BOOLEAN,
    execution_time_ms INTEGER,
    memory_used_kb INTEGER,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES submission_answers(answer_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_submissions_assessment_id ON submissions(assessment_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_submission_answers_submission_id ON submission_answers(submission_id);
CREATE INDEX idx_submission_answers_question_id ON submission_answers(question_id);
CREATE INDEX idx_code_executions_submission_id ON code_executions(submission_id);
CREATE INDEX idx_test_case_results_answer_id ON test_case_results(answer_id);

-- Create function to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to update updated_at on all tables
CREATE TRIGGER update_submissions_updated_at
BEFORE UPDATE ON submissions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_submission_answers_updated_at
BEFORE UPDATE ON submission_answers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert sample submission data
INSERT INTO submissions (
    assessment_id,
    user_id,
    status,
    started_at,
    submitted_at,
    time_taken_seconds,
    score,
    max_score,
    percentage_score,
    is_passed,
    ip_address,
    user_agent
) VALUES (
    1, -- assessment_id
    1001, -- user_id
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    1800, -- 30 minutes
    75.0, -- score
    100.0, -- max_score
    75.0, -- percentage_score
    true, -- is_passed
    '192.168.1.100',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
) RETURNING submission_id;

-- Insert sample submission answers
WITH inserted_submission AS (
    SELECT submission_id FROM submissions WHERE user_id = 1001 AND assessment_id = 1 LIMIT 1
)
INSERT INTO submission_answers (
    submission_id,
    question_id,
    question_type,
    user_answer,
    is_correct,
    points_awarded,
    max_points,
    time_taken_seconds
)
SELECT 
    submission_id,
    1, -- question_id
    'MCQ',
    'A', -- user_answer
    true, -- is_correct
    10.0, -- points_awarded
    10.0, -- max_points
    60 -- time_taken_seconds
FROM inserted_submission
UNION ALL
SELECT 
    submission_id,
    2, -- question_id
    'CODING',
    'function sum(a, b) { return a + b; }', -- user_answer
    true, -- is_correct
    15.0, -- points_awarded
    20.0, -- max_points
    300 -- time_taken_seconds
FROM inserted_submission;

-- Insert sample code execution
WITH inserted_answer AS (
    SELECT sa.answer_id, s.submission_id 
    FROM submission_answers sa
    JOIN submissions s ON sa.submission_id = s.submission_id
    WHERE sa.question_type = 'CODING' AND s.user_id = 1001
    LIMIT 1
)
INSERT INTO code_executions (
    submission_id,
    answer_id,
    language,
    source_code,
    stdin,
    stdout,
    stderr,
    exit_code,
    time_taken_ms,
    memory_used_kb,
    status
)
SELECT 
    submission_id,
    answer_id,
    'javascript',
    'function sum(a, b) { return a + b; }',
    '{"a": 5, "b": 3}',
    '8',
    '',
    0,
    100,
    2048,
    'SUCCESS'
FROM inserted_answer;

-- Insert sample test case results
WITH inserted_answer AS (
    SELECT sa.answer_id 
    FROM submission_answers sa
    JOIN submissions s ON sa.submission_id = s.submission_id
    WHERE sa.question_type = 'CODING' AND s.user_id = 1001
    LIMIT 1
)
INSERT INTO test_case_results (
    answer_id,
    test_case_id,
    input_data,
    expected_output,
    actual_output,
    is_passed,
    execution_time_ms,
    memory_used_kb
)
SELECT 
    answer_id,
    1, -- test_case_id
    '{"a": 1, "b": 2}', -- input_data
    '3', -- expected_output
    '3', -- actual_output
    true, -- is_passed
    50, -- execution_time_ms
    1024 -- memory_used_kb
FROM inserted_answer
UNION ALL
SELECT 
    answer_id,
    2, -- test_case_id
    '{"a": -1, "b": 1}', -- input_data
    '0', -- expected_output
    '0', -- actual_output
    true, -- is_passed
    45, -- execution_time_ms
    1024 -- memory_used_kb
FROM inserted_answer;

-- Create a read-only user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'submission_ro') THEN
        CREATE ROLE submission_ro LOGIN PASSWORD 'readonlypass';
    END IF;
    
    GRANT USAGE ON SCHEMA submission_schema TO submission_ro;
    GRANT SELECT ON ALL TABLES IN SCHEMA submission_schema TO submission_ro;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA submission_schema 
    GRANT SELECT ON TABLES TO submission_ro;
END $$;

-- Create a read-write user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'submission_rw') THEN
        CREATE ROLE submission_rw LOGIN PASSWORD 'readwritepass';
    END IF;
    
    GRANT USAGE, CREATE ON SCHEMA submission_schema TO submission_rw;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA submission_schema TO submission_rw;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA submission_schema TO submission_rw;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA submission_schema 
    GRANT ALL PRIVILEGES ON TABLES TO submission_rw;
    
    ALTER DEFAULT PRIVILEGES IN SCHEMA submission_schema
    GRANT ALL PRIVILEGES ON SEQUENCES TO submission_rw;
END $$;

-- Print completion message
\echo '\nDatabase initialization completed successfully!\n';
\echo 'Sample data has been loaded:';
\echo '  - 1 sample submission with status COMPLETED';
\echo '  - 2 sample answers (1 MCQ, 1 CODING)';
\echo '  - 1 code execution record';
\echo '  - 2 test case results\n';
\echo 'Database users created:';
\echo '  - submission_ro (read-only access)';
\echo '  - submission_rw (read-write access)\n';
\echo 'To connect to the database:';
\echo '  psql -h localhost -U submission_rw -d your_database_name';
\echo '  or';
\echo '  psql -h localhost -U submission_ro -d your_database_name\n';
