# Question Service - Progress Report

**Service Name:** QuestionService  
**Port:** 8082  
**Database:** PostgreSQL  
**Last Updated:** October 24, 2025  

---

## Overview
The Question Service manages sections, questions, MCQ options, and test cases for coding questions. It provides a comprehensive question bank for assessments with support for multiple question types.

---

## Functionalities Implemented

### 1. Section Management
**Endpoints:**
- `POST /sections` - Create a new section
- `GET /sections` - Get all sections
- `GET /sections/{id}` - Get section by ID
- `PUT /sections/{id}` - Update section
- `DELETE /sections/{id}` - Delete section

**Features:**
- Create and organize questions into sections (DSA, CS Fundamentals, Aptitude, etc.)
- Section ordering with display_order field
- Section descriptions
- Hierarchical organization of questions

---

### 2. Question Management
**Endpoints:**
- `POST /sections/{sectionId}/questions` - Create question in a section
- `GET /sections/{sectionId}/questions` - Get all questions in a section
- `GET /questions/{id}` - Get question by ID with options/test cases
- `PUT /questions/{id}` - Update question
- `DELETE /questions/{id}` - Delete question
- `GET /questions/bulk` - Get multiple questions by IDs

**Features:**
- Support for multiple question types (MCQ, CODING)
- Difficulty levels (EASY, MEDIUM, HARD)
- Points/scoring system per question
- Time limits per question
- Code templates for coding questions
- Programming language specification
- Rich text support for question text

**Question Types:**
- **MCQ** - Multiple Choice Questions with options
- **CODING** - Programming questions with test cases

---

### 3. MCQ Options Management
**Endpoints:**
- Managed through question endpoints
- Options created/updated with question

**Features:**
- Multiple options per MCQ question
- Correct answer marking
- Option ordering
- Automatic cascade on question deletion

---

### 4. Test Cases Management
**Endpoints:**
- Managed through question endpoints
- Test cases created/updated with question

**Features:**
- Input/output test cases for coding questions
- Hidden test cases (not visible to candidates)
- Sample test cases (visible to candidates)
- Expected output storage
- Automatic cascade on question deletion

---

## Database Schema

### Tables

#### 1. **sections**
```sql
CREATE TABLE sections (
    section_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**
- `section_id` - Primary key
- `name` - Section name (e.g., "DSA", "CS Fundamentals")
- `description` - Section description
- `display_order` - Order for display in UI
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

**Common Section Names:**
- Data Structures and Algorithms (DSA)
- CS Fundamentals
- Aptitude
- Programming Basics
- System Design

---

#### 2. **questions**
```sql
CREATE TABLE questions (
    question_id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50),
    points INTEGER,
    time_limit_minutes INTEGER,
    code_template TEXT,
    programming_language VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
);
```

**Columns:**
- `question_id` - Primary key
- `section_id` - Foreign key to sections
- `question_text` - Question content (supports HTML/Markdown)
- `type` - Enum: MCQ, CODING
- `difficulty_level` - Enum: EASY, MEDIUM, HARD
- `points` - Points awarded for correct answer
- `time_limit_minutes` - Time limit for this question
- `code_template` - Initial code template for coding questions
- `programming_language` - Language for coding questions (Java, Python, C++, JavaScript)
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

**Type Enum Values:**
- `MCQ` - Multiple Choice Question
- `CODING` - Programming/Coding Question

**Difficulty Level Enum Values:**
- `EASY` - Beginner level
- `MEDIUM` - Intermediate level
- `HARD` - Advanced level

---

#### 3. **mcq_options**
```sql
CREATE TABLE mcq_options (
    option_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    option_order INTEGER,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);
```

**Columns:**
- `option_id` - Primary key
- `question_id` - Foreign key to questions
- `option_text` - Option content
- `is_correct` - Whether this is the correct answer
- `option_order` - Display order of option

**Notes:**
- Multiple options per MCQ question (typically 4)
- Only one option should have `is_correct = true`
- Cascade delete when question is deleted

---

#### 4. **test_cases**
```sql
CREATE TABLE test_cases (
    test_case_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_sample BOOLEAN DEFAULT FALSE,
    test_case_order INTEGER,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);
```

**Columns:**
- `test_case_id` - Primary key
- `question_id` - Foreign key to questions
- `input` - Test case input
- `expected_output` - Expected output for the input
- `is_sample` - Whether visible to candidate as sample
- `test_case_order` - Display order

**Notes:**
- Multiple test cases per coding question
- Sample test cases shown to candidates for testing
- Hidden test cases used for evaluation only
- Cascade delete when question is deleted

---

## Question Creation Flow

### Creating an MCQ Question
```json
POST /sections/1/questions
{
  "questionText": "What is the time complexity of binary search?",
  "type": "MCQ",
  "difficultyLevel": "EASY",
  "points": 10,
  "timeLimitMinutes": 2,
  "mcqOptions": [
    {
      "optionText": "O(n)",
      "isCorrect": false,
      "optionOrder": 1
    },
    {
      "optionText": "O(log n)",
      "isCorrect": true,
      "optionOrder": 2
    },
    {
      "optionText": "O(n²)",
      "isCorrect": false,
      "optionOrder": 3
    },
    {
      "optionText": "O(1)",
      "isCorrect": false,
      "optionOrder": 4
    }
  ]
}
```

### Creating a Coding Question
```json
POST /sections/1/questions
{
  "questionText": "Write a function to reverse a string",
  "type": "CODING",
  "difficultyLevel": "EASY",
  "points": 20,
  "timeLimitMinutes": 15,
  "programmingLanguage": "Java",
  "codeTemplate": "public String reverseString(String str) {\n    // Your code here\n}",
  "testCases": [
    {
      "input": "hello",
      "expectedOutput": "olleh",
      "isSample": true,
      "testCaseOrder": 1
    },
    {
      "input": "world",
      "expectedOutput": "dlrow",
      "isSample": false,
      "testCaseOrder": 2
    }
  ]
}
```

---

## Integration Points

### With AssessmentService
- Assessment structure references question IDs
- Questions fetched by IDs during assessment attempts
- Bulk fetch API for efficient loading

### With SubmissionService
- Test cases used for code evaluation
- MCQ correct answers used for scoring
- Question points used in score calculation

### With Frontend
- Question display during assessment
- Code editor with templates
- MCQ option rendering

---

## Key Features

### ✅ Question Types Support
1. **MCQ Questions**
   - Multiple options with single correct answer
   - Instant evaluation possible
   - No execution required

2. **Coding Questions**
   - Code templates for different languages
   - Test case validation
   - Supports multiple programming languages
   - Sample vs hidden test cases

### ✅ Difficulty Management
- Three difficulty levels
- Helps in assessment balancing
- Filters for question selection

### ✅ Section Organization
- Logical grouping of questions
- Easy navigation and management
- Reusable across assessments

### ✅ Bulk Operations
- Fetch multiple questions at once
- Efficient for assessment loading
- Reduces API calls

---

## API Response Examples

### Get Question with Details
```json
GET /questions/123

Response:
{
  "questionId": 123,
  "sectionId": 1,
  "questionText": "What is the time complexity of binary search?",
  "type": "MCQ",
  "difficultyLevel": "EASY",
  "points": 10,
  "timeLimitMinutes": 2,
  "mcqOptions": [
    {
      "optionId": 1,
      "optionText": "O(n)",
      "isCorrect": false,
      "optionOrder": 1
    },
    {
      "optionId": 2,
      "optionText": "O(log n)",
      "isCorrect": true,
      "optionOrder": 2
    }
  ],
  "createdAt": "2025-10-24T10:00:00Z",
  "updatedAt": "2025-10-24T10:00:00Z"
}
```

---

## Technology Stack
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Validation:** Jakarta Validation
- **JSON Processing:** Jackson
- **CORS:** Configured for frontend access

---

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8082

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/question_db
spring.datasource.username=postgres
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# CORS Configuration
cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

---

## Supported Programming Languages
- ☑️ Java
- ☑️ Python
- ☑️ C++
- ☑️ JavaScript
- 🔄 C# (Planned)
- 🔄 Go (Planned)

---

## Pending Enhancements
- [ ] Question versioning
- [ ] Question tags/categories
- [ ] Question difficulty auto-calculation based on success rate
- [ ] Question pool randomization
- [ ] Rich text editor integration
- [ ] Image/diagram support in questions
- [ ] Code snippet highlighting in question text
- [ ] Question import/export (CSV, JSON)
- [ ] Question templates
- [ ] Question review and approval workflow
- [ ] Question analytics (usage, success rate)
- [ ] Multi-language support for questions
- [ ] Explanation field for correct answers

---

## Error Handling
- **400 Bad Request:** Invalid question data
- **404 Not Found:** Question/Section not found
- **409 Conflict:** Duplicate question
- **500 Internal Server Error:** Database issues

---

**Status:** ✅ Production Ready  
**Test Coverage:** Partial (manual testing completed)  
**Question Bank Size:** Growing (Admin-managed)

