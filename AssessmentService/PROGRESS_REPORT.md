# Assessment Service - Progress Report

**Service Name:** AssessmentService  
**Port:** 8081  
**Database:** PostgreSQL  
**Last Updated:** October 24, 2025  

---

## Overview
The Assessment Service manages companies, assessments, candidates, and assessment attempts. It handles the complete lifecycle of assessments from creation to candidate enrollment and attempt tracking.

---

## Functionalities Implemented

### 1. Company Management
**Endpoints:**
- `POST /companies` - Create a new company
- `GET /companies` - Get all companies
- `GET /companies/{id}` - Get company by ID
- `PUT /companies/{id}` - Update company
- `DELETE /companies/{id}` - Delete company

**Features:**
- Create and manage companies/organizations
- Store company metadata (name, description, domain)
- Track company assessments

---

### 2. Assessment Management
**Endpoints:**
- `POST /companies/{companyId}/assessments` - Create assessment for a company
- `GET /companies/{companyId}/assessments` - Get all assessments for a company
- `GET /assessments/{assessmentId}` - Get assessment by ID
- `PUT /assessments/{assessmentId}` - Update assessment
- `DELETE /assessments/{assessmentId}` - Delete assessment
- `GET /assessments/{assessmentId}/structure` - Get assessment structure with sections

**Features:**
- Create assessments with structured sections (MCQ, Coding, etc.)
- Store assessment metadata (name, description, duration, schedule)
- JSON-based structure for flexible assessment configuration
- Link assessments to companies
- Support for multiple question types and sections

---

### 3. Candidate Management
**Endpoints:**
- `POST /assessments/{assessmentId}/candidates` - Add candidate to assessment
- `GET /assessments/{assessmentId}/candidates` - Get all candidates for assessment
- `PUT /assessments/{assessmentId}/candidates/{candidateId}` - Update candidate status
- `DELETE /assessments/{assessmentId}/candidates/{candidateId}` - Remove candidate
- `PUT /assessments/candidates/{candidateId}/score` - Update candidate score (called by SubmissionService)
- `GET /assessments/candidates/user/{userRef}` - Get all candidates for a user
- `GET /assessments/user/{userRef}/attempts` - Get user's attempted assessments

**Features:**
- Enroll candidates in assessments
- Track candidate status (INVITED, IN_PROGRESS, SUBMITTED, EVALUATED)
- Store candidate answers (JSONB format)
- Track timing information (start time, completion time, time taken)
- Store evaluation results and scores
- Denormalized fields for quick access (assessmentName, companyName)
- **Automatic name formatting** - Converts snake_case names to Title Case

---

### 4. Assessment Attempt Management
**Endpoints:**
- `POST /assessments/{assessmentId}/attempt` - Start an assessment attempt
- `GET /assessments/{assessmentId}/attempt` - Get assessment attempt data
- `POST /assessments/{assessmentId}/submit` - Submit assessment answers
- `PUT /assessments/{assessmentId}/progress` - Update candidate progress during assessment
- `GET /assessments/{assessmentId}/results` - Get assessment results and analytics

**Features:**
- Start assessment attempts for candidates
- Track assessment progress in real-time
- Handle answer submission
- Fetch questions and structure for assessment taking
- Generate assessment results with detailed analytics
- Support for auto-save/progress tracking

---

## Database Schema

### Tables

#### 1. **companies**
```sql
CREATE TABLE companies (
    company_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    domain VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**
- `company_id` - Primary key
- `name` - Company name
- `description` - Company description
- `domain` - Unique company domain/identifier
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

---

#### 2. **assessments**
```sql
CREATE TABLE assessments (
    assessment_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(255),
    scheduled_at TIMESTAMP,
    duration_minutes INTEGER,
    structure JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id)
);
```

**Columns:**
- `assessment_id` - Primary key
- `company_id` - Foreign key to companies
- `name` - Assessment name
- `description` - Assessment description
- `created_by` - Creator identifier
- `scheduled_at` - Scheduled date/time
- `duration_minutes` - Time limit in minutes
- `structure` - JSON structure with sections and question IDs
- `created_at` - Creation timestamp

**Structure Format Example:**
```json
{
  "sections": [
    {
      "sectionId": 1,
      "sectionName": "DSA",
      "questionIds": [101, 102, 103]
    },
    {
      "sectionId": 2,
      "sectionName": "Coding",
      "questionIds": [201, 202]
    }
  ]
}
```

---

#### 3. **assessment_candidates**
```sql
CREATE TABLE assessment_candidates (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    user_ref INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INVITED',
    
    -- Denormalized fields
    assessment_name VARCHAR(255),
    company_name VARCHAR(255),
    
    -- Timing fields
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    time_remaining_minutes INTEGER,
    time_taken_minutes INTEGER,
    
    -- Answer and scoring fields
    answers JSONB,
    total_score DOUBLE PRECISION,
    max_score DOUBLE PRECISION,
    percentage_score DOUBLE PRECISION,
    is_passed BOOLEAN,
    
    -- Question statistics
    total_questions INTEGER,
    attempted_questions INTEGER,
    correct_answers INTEGER,
    incorrect_answers INTEGER,
    unanswered_questions INTEGER,
    
    -- Question type statistics
    mcq_attempted INTEGER,
    mcq_correct INTEGER,
    coding_attempted INTEGER,
    coding_passed INTEGER,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id),
    UNIQUE(assessment_id, user_ref)
);
```

**Columns:**
- `id` - Primary key
- `assessment_id` - Foreign key to assessments
- `user_ref` - User identifier (hash of email from AuthService)
- `status` - Enum: INVITED, IN_PROGRESS, SUBMITTED, EVALUATED
- `assessment_name` - Denormalized formatted assessment name
- `company_name` - Denormalized company name
- `started_at` - When candidate started the assessment
- `completed_at` - When candidate completed the assessment
- `time_remaining_minutes` - Remaining time (for pause/resume)
- `time_taken_minutes` - Total time taken
- `answers` - JSON object with candidate's answers
- `total_score` - Total score achieved
- `max_score` - Maximum possible score
- `percentage_score` - Score as percentage
- `is_passed` - Whether candidate passed
- `total_questions` - Total questions in assessment
- `attempted_questions` - Number of attempted questions
- `correct_answers` - Number of correct answers
- `incorrect_answers` - Number of incorrect answers
- `unanswered_questions` - Number of unanswered questions
- `mcq_attempted` - MCQ questions attempted
- `mcq_correct` - MCQ questions correct
- `coding_attempted` - Coding questions attempted
- `coding_passed` - Coding questions passed
- `created_at` - Creation timestamp

**Status Enum Values:**
- `INVITED` - Candidate invited but not started
- `IN_PROGRESS` - Assessment currently in progress
- `SUBMITTED` - Assessment submitted, awaiting evaluation
- `EVALUATED` - Assessment evaluated with final scores

---

## Key Features & Enhancements

### ✅ Recently Added Features

1. **Assessment Name Formatting**
   - Automatic conversion of database names (e.g., `test_1_assessment`) to formatted names (e.g., `Test 1 Assessment`)
   - Stored in denormalized `assessmentName` field for performance
   - Fallback formatting on frontend if needed

2. **Score Update Integration**
   - Endpoint for SubmissionService to update candidate scores after evaluation
   - Comprehensive score tracking (total, MCQ, coding)
   - Detailed question-level statistics

3. **User Dashboard Support**
   - API to fetch all attempted assessments for a user
   - Sorted by most recent first
   - Includes formatted names and company information

4. **Lazy Loading Optimization**
   - Explicit loading of related entities to avoid N+1 queries
   - Better performance for user dashboard and assessment listing

---

## Integration Points

### With QuestionService
- Fetches questions by section IDs
- Validates question existence during assessment structure creation

### With SubmissionService
- Receives score updates after evaluation
- Provides candidate and assessment details for evaluation

### With AuthService
- Uses user references (email hash) for candidate identification
- No direct API integration (loose coupling)

### With Frontend
- Dashboard displays user's attempted assessments
- Assessment attempt page for taking tests
- Admin panel for assessment management

---

## Technology Stack
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL 15+
- **ORM:** JPA/Hibernate
- **JSON Support:** JSONB for flexible data storage
- **Validation:** Jakarta Validation
- **Mapping:** MapStruct for DTOs

---

## Pending Enhancements
- [ ] Email notifications for assessment invitations
- [ ] Assessment templates for reuse
- [ ] Bulk candidate import
- [ ] Advanced analytics and reporting
- [ ] Assessment versioning
- [ ] Proctoring features integration
- [ ] Assessment sharing between companies

---

## API Documentation
Refer to Swagger UI at: `http://localhost:8081/swagger-ui.html` (when implemented)

---

**Status:** ✅ Production Ready  
**Test Coverage:** Partial (manual testing completed)

