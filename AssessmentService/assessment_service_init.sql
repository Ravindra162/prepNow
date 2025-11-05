-- Assessment Service Database Initialization Script
-- This script will create the schema, tables, indexes, and sample data

-- Drop existing schema and all objects (WARNING: This will delete all data!)
DROP SCHEMA IF EXISTS assessment_schema CASCADE;

-- Create schema for Assessment Service
CREATE SCHEMA assessment_schema;

-- Set search path to the new schema
SET search_path TO assessment_schema;

-- Create companies table
CREATE TABLE companies (
    company_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    domain VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create assessments table
CREATE TABLE assessments (
    assessment_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    scheduled_at TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER,
    structure JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    max_score DOUBLE PRECISION,
    total_questions INTEGER,
    is_active BOOLEAN DEFAULT true,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

-- Create assessment_candidates table
CREATE TABLE assessment_candidates (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    user_ref INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INVITED',
    assessment_name VARCHAR(255),
    company_name VARCHAR(255),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    time_remaining_minutes INTEGER,
    time_taken_minutes INTEGER,
    answers JSONB,
    total_score DOUBLE PRECISION,
    max_score DOUBLE PRECISION,
    percentage_score DOUBLE PRECISION,
    is_passed BOOLEAN,
    total_questions INTEGER,
    attempted_questions INTEGER,
    correct_answers INTEGER,
    incorrect_answers INTEGER,
    unanswered_questions INTEGER,
    mcq_attempted INTEGER,
    mcq_correct INTEGER,
    coding_attempted INTEGER,
    coding_passed INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE,
    UNIQUE(assessment_id, user_ref)
);

-- Create indexes for better performance
CREATE INDEX idx_companies_domain ON companies(domain);
CREATE INDEX idx_assessments_company_id ON assessments(company_id);
CREATE INDEX idx_assessments_scheduled_at ON assessments(scheduled_at);
CREATE INDEX idx_assessment_candidates_assessment_id ON assessment_candidates(assessment_id);
CREATE INDEX idx_assessment_candidates_user_ref ON assessment_candidates(user_ref);
CREATE INDEX idx_assessment_candidates_status ON assessment_candidates(status);

-- Create function to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update updated_at on companies table
CREATE TRIGGER update_companies_updated_at
BEFORE UPDATE ON companies
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create trigger to update updated_at on assessment_candidates table
CREATE TRIGGER update_assessment_candidates_updated_at
BEFORE UPDATE ON assessment_candidates
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert sample companies
INSERT INTO companies (name, description, domain) VALUES
('TechCorp', 'Leading technology solutions provider', 'techcorp.com'),
('DataSystems', 'Data analytics and business intelligence', 'datasystems.io'),
('CloudNova', 'Cloud infrastructure services', 'cloudnova.tech')
ON CONFLICT (domain) DO NOTHING;

-- Insert sample assessments
WITH inserted_company AS (
    SELECT company_id FROM companies WHERE domain = 'techcorp.com' LIMIT 1
)
INSERT INTO assessments (
    company_id, 
    name, 
    description, 
    created_by, 
    scheduled_at, 
    duration_minutes, 
    structure,
    max_score,
    total_questions,
    is_active
) 
SELECT 
    company_id,
    'Full Stack Developer Assessment',
    'Comprehensive assessment for full stack developer position',
    'admin@techcorp.com',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    120,
    '{"sections": [{"id": 1, "name": "JavaScript", "weight": 30}, {"id": 2, "name": "React", "weight": 30}, {"id": 3, "name": "Node.js", "weight": 40}]}'::jsonb,
    100.0,
    30,
    true
FROM inserted_company
ON CONFLICT DO NOTHING;

-- Insert sample candidate
WITH inserted_assessment AS (
    SELECT a.assessment_id, c.name as company_name, a.name as assessment_name, a.max_score, a.total_questions
    FROM assessments a
    JOIN companies c ON a.company_id = c.company_id
    WHERE c.domain = 'techcorp.com'
    LIMIT 1
)
INSERT INTO assessment_candidates (
    assessment_id,
    user_ref,
    status,
    assessment_name,
    company_name,
    started_at,
    completed_at,
    time_taken_minutes,
    total_score,
    max_score,
    percentage_score,
    is_passed,
    total_questions,
    attempted_questions,
    correct_answers,
    incorrect_answers,
    unanswered_questions,
    mcq_attempted,
    mcq_correct,
    coding_attempted,
    coding_passed
)
SELECT 
    assessment_id,
    1001,
    'COMPLETED',
    assessment_name,
    company_name,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    90,
    85.0,
    max_score,
    85.0,
    true,
    total_questions,
    28,
    25,
    3,
    total_questions - 28,
    25,
    25,
    3,
    2
FROM inserted_assessment
ON CONFLICT DO NOTHING;

-- Create a read-only user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'assessment_ro') THEN
        CREATE ROLE assessment_ro LOGIN PASSWORD 'readonlypass';
    END IF;
    
    GRANT USAGE ON SCHEMA assessment_schema TO assessment_ro;
    GRANT SELECT ON ALL TABLES IN SCHEMA assessment_schema TO assessment_ro;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA assessment_schema 
    GRANT SELECT ON TABLES TO assessment_ro;
END $$;

-- Create a read-write user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'assessment_rw') THEN
        CREATE ROLE assessment_rw LOGIN PASSWORD 'readwritepass';
    END IF;
    
    GRANT USAGE, CREATE ON SCHEMA assessment_schema TO assessment_rw;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA assessment_schema TO assessment_rw;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA assessment_schema TO assessment_rw;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA assessment_schema 
    GRANT ALL PRIVILEGES ON TABLES TO assessment_rw;
    
    ALTER DEFAULT PRIVILEGES IN SCHEMA assessment_schema
    GRANT ALL PRIVILEGES ON SEQUENCES TO assessment_rw;
END $$;

-- Print completion message
\echo '\nDatabase initialization completed successfully!\n';
\echo 'Sample data has been loaded:';
\echo '  - 3 sample companies';
\echo '  - 1 sample assessment';
\echo '  - 1 sample candidate record\n';
\echo 'Database users created:';
\echo '  - assessment_ro (read-only access)';
\echo '  - assessment_rw (read-write access)\n';
\echo 'To connect to the database:';
\echo '  psql -h localhost -U assessment_rw -d your_database_name';
\echo '  or';
\echo '  psql -h localhost -U assessment_ro -d your_database_name\n';
