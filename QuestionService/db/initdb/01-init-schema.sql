-- Create schema for Question Service
CREATE SCHEMA IF NOT EXISTS question_schema;

-- Set search path to the new schema
SET search_path TO question_schema;

-- Create sections table
CREATE TABLE IF NOT EXISTS sections (
    section_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create questions table
CREATE TABLE IF NOT EXISTS questions (
    question_id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50),
    points INTEGER DEFAULT 10,
    time_limit_minutes INTEGER,
    code_template TEXT,
    programming_language VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

-- Create mcq_options table
CREATE TABLE IF NOT EXISTS mcq_options (
    option_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    option_label VARCHAR(10) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Create test_cases table
CREATE TABLE IF NOT EXISTS test_cases (
    test_case_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    input_data TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_sample BOOLEAN NOT NULL DEFAULT false,
    test_case_order INTEGER,
    description TEXT,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_sections_display_order ON sections(display_order);
CREATE INDEX IF NOT EXISTS idx_questions_section_id ON questions(section_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);
CREATE INDEX IF NOT EXISTS idx_questions_difficulty_level ON questions(difficulty_level);
CREATE INDEX IF NOT EXISTS idx_mcq_options_question_id ON mcq_options(question_id);
CREATE INDEX IF NOT EXISTS idx_mcq_options_is_correct ON mcq_options(is_correct);
CREATE INDEX IF NOT EXISTS idx_test_cases_question_id ON test_cases(question_id);
CREATE INDEX IF NOT EXISTS idx_test_cases_is_sample ON test_cases(is_sample);

-- Create function to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to update updated_at on sections table
DROP TRIGGER IF EXISTS update_sections_updated_at ON question_schema.sections;
CREATE TRIGGER update_sections_updated_at
BEFORE UPDATE ON question_schema.sections
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create trigger to update updated_at on questions table
DROP TRIGGER IF EXISTS update_questions_updated_at ON question_schema.questions;
CREATE TRIGGER update_questions_updated_at
BEFORE UPDATE ON question_schema.questions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert default sections if they don't exist
INSERT INTO sections (name, description, display_order)
VALUES
    ('General', 'General questions', 1),
    ('Programming', 'Programming and coding questions', 2),
    ('Algorithms', 'Algorithm and data structure questions', 3),
    ('Database', 'Database and SQL questions', 4),
    ('Web Development', 'Web development questions', 5)
ON CONFLICT DO NOTHING;

