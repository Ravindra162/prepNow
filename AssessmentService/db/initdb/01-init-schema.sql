-- Create schema for Assessment Service
CREATE SCHEMA IF NOT EXISTS assessment_schema;

-- Set search path to the new schema
SET search_path TO assessment_schema;

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    company_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    domain VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create assessments table
CREATE TABLE IF NOT EXISTS assessments (
    assessment_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    scheduled_at TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER,
    structure JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

-- Create assessment_candidates table
CREATE TABLE IF NOT EXISTS assessment_candidates (
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
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_companies_domain ON companies(domain);
CREATE INDEX IF NOT EXISTS idx_assessments_company_id ON assessments(company_id);
CREATE INDEX IF NOT EXISTS idx_assessments_scheduled_at ON assessments(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_assessment_candidates_assessment_id ON assessment_candidates(assessment_id);
CREATE INDEX IF NOT EXISTS idx_assessment_candidates_user_ref ON assessment_candidates(user_ref);
CREATE INDEX IF NOT EXISTS idx_assessment_candidates_status ON assessment_candidates(status);

-- Create function to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update updated_at on companies table
DROP TRIGGER IF EXISTS update_companies_updated_at ON assessment_schema.companies;
CREATE TRIGGER update_companies_updated_at
BEFORE UPDATE ON assessment_schema.companies
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert sample company if it doesn't exist
INSERT INTO companies (name, description, domain)
VALUES ('Default Company', 'Default company for testing', 'default.com')
ON CONFLICT (domain) DO NOTHING;

