# Auth Service - Progress Report

**Service Name:** AuthService  
**Port:** 8080  
**Database:** PostgreSQL  
**Last Updated:** October 24, 2025  

---

## Overview
The Authentication Service handles user registration, login, email verification, and JWT token management. It provides secure authentication for the entire application.

---

## Functionalities Implemented

### 1. User Registration
**Endpoint:**
- `POST /auth/register` - Register new user

**Features:**
- User registration with email and password
- Password encryption using BCrypt
- Email uniqueness validation
- Automatic OTP generation for email verification
- Email sending for verification

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "message": "User registered successfully. Please verify your email.",
  "userId": 1
}
```

---

### 2. Email Verification
**Endpoints:**
- `POST /auth/verify-email` - Verify email with OTP
- `POST /auth/resend-otp` - Resend OTP to email

**Features:**
- OTP-based email verification
- OTP expires after 10 minutes
- OTP resend functionality
- Account activation upon successful verification

**Verify Request:**
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

---

### 3. User Login
**Endpoint:**
- `POST /auth/login` - User login

**Features:**
- Email and password authentication
- Email verification check
- JWT token generation
- Secure password validation using BCrypt

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "username": "john_doe",
  "id": 1
}
```

---

### 4. Token Validation
**Endpoint:**
- `GET /auth/validate` - Validate JWT token

**Features:**
- JWT token validation
- Token expiration check
- User details extraction from token

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "valid": true,
  "email": "john@example.com"
}
```

---

## Database Schema

### Table: **users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE
);
```

**Columns:**
- `id` - Primary key, auto-generated
- `username` - User's display name
- `email` - Unique email address (used for login)
- `password` - BCrypt hashed password
- `is_email_verified` - Email verification status

**Indexes:**
- Unique index on `email` for fast lookup and uniqueness

---

## Security Features

### 1. Password Encryption
- **Algorithm:** BCrypt with strength 10
- **Salt:** Auto-generated per password
- Secure password hashing before storage

### 2. JWT Token Management
- **Algorithm:** HS256
- **Secret Key:** Configurable in application.properties
- **Expiration:** Configurable (default: 24 hours)
- **Claims:** email, username, userId

### 3. Email Verification
- **OTP Length:** 6 digits
- **Expiration:** 10 minutes
- **Storage:** In-memory with expiration tracking
- **Security:** OTP sent only to registered email

### 4. Spring Security Configuration
- **CORS:** Enabled for frontend (localhost:5173, localhost:3000)
- **CSRF:** Disabled for stateless JWT authentication
- **Public Endpoints:** /auth/**, /h2-console/** (if H2 enabled)
- **Protected Endpoints:** JWT filter validates all other endpoints

---

## Key Components

### 1. JwtService
**Responsibilities:**
- Generate JWT tokens
- Extract email from token
- Validate token expiration
- Verify token signature

**Key Methods:**
```java
String generateToken(String email)
String extractEmail(String token)
Boolean validateToken(String token, String email)
```

---

### 2. OtpService
**Responsibilities:**
- Generate 6-digit random OTP
- Store OTP with expiration (10 minutes)
- Validate OTP
- Clean up expired OTPs

**Key Methods:**
```java
String generateOtp(String email)
boolean validateOtp(String email, String otp)
void invalidateOtp(String email)
```

---

### 3. EmailService
**Responsibilities:**
- Send verification emails
- SMTP configuration
- Email templating

**Configuration:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
```

---

### 4. UserService
**Responsibilities:**
- User CRUD operations
- Password encryption
- Email verification status management
- User authentication

---

## Integration Points

### With Frontend
- Login page authentication
- Registration flow with email verification
- Token storage in localStorage/sessionStorage
- Token refresh mechanism

### With Other Microservices
- **Loose Coupling:** Uses email hash as userRef
- **No Direct API Calls:** Services identify users by email hash
- **Future Enhancement:** User profile service integration

---

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## API Flow Examples

### Registration Flow
```
1. POST /auth/register
   ↓
2. User created (isEmailVerified = false)
   ↓
3. OTP generated and stored
   ↓
4. Email sent with OTP
   ↓
5. User enters OTP
   ↓
6. POST /auth/verify-email
   ↓
7. OTP validated
   ↓
8. isEmailVerified set to true
   ↓
9. User can now login
```

### Login Flow
```
1. POST /auth/login
   ↓
2. Email and password validated
   ↓
3. Check isEmailVerified = true
   ↓
4. Generate JWT token
   ↓
5. Return token + user details
   ↓
6. Frontend stores token
   ↓
7. Token sent in Authorization header for protected routes
```

---

## Error Handling

### Common Errors
- **400 Bad Request:** Invalid input data
- **401 Unauthorized:** Invalid credentials or token
- **403 Forbidden:** Email not verified
- **409 Conflict:** Email already registered
- **500 Internal Server Error:** Server-side issues

### Error Response Format
```json
{
  "error": "Email already exists",
  "status": 409,
  "timestamp": "2025-10-24T10:30:00Z"
}
```

---

## Technology Stack
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security 6.x
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **JWT:** io.jsonwebtoken (JJWT)
- **Email:** Spring Mail
- **Password Encryption:** BCrypt

---

## Pending Enhancements
- [ ] Password reset functionality
- [ ] Social login (Google, GitHub)
- [ ] Two-factor authentication (2FA)
- [ ] Refresh token mechanism
- [ ] User profile management
- [ ] Role-based access control (RBAC)
- [ ] Account lockout after failed attempts
- [ ] Password strength validation
- [ ] Email verification link (in addition to OTP)
- [ ] Session management

---

## Security Best Practices Implemented
✅ Password hashing with BCrypt  
✅ JWT token-based authentication  
✅ Email verification required  
✅ CORS configuration  
✅ Stateless authentication  
✅ Token expiration  
✅ OTP expiration (10 minutes)  

---

**Status:** ✅ Production Ready  
**Test Coverage:** Partial (manual testing completed)

