# Auth Service Schema Migration - Admin & User Separation

## ✅ Migration Completed Successfully!

The database schema has been updated to support proper admin and user role separation with enhanced security features.

---

## 📊 New Database Schema

### 1. **ROLES Table**
Stores role definitions for the system.

| Column      | Type      | Description                    |
|-------------|-----------|--------------------------------|
| id          | BIGSERIAL | Primary key                    |
| name        | VARCHAR   | Role name (ADMIN, USER)        |
| description | TEXT      | Role description               |
| created_at  | TIMESTAMP | Creation timestamp             |
| updated_at  | TIMESTAMP | Last update timestamp          |

**Default Roles:**
- `ADMIN` - Administrator with full system access
- `USER` - Regular user with limited access

---

### 2. **USERS Table (Enhanced)**
Main user table with role-based access and security features.

#### Core Fields:
- `id` - Primary key
- `username` - Unique username (min 3 characters)
- `email` - Unique email with format validation
- `password` - Encrypted password

#### Role & Status:
- `role` - ENUM ('ADMIN', 'USER') - Default: 'USER'
- `role_id` - Foreign key to roles table
- `is_active` - Account active status
- `is_locked` - Account lock status for security
- `failed_login_attempts` - Track failed login attempts
- `locked_until` - Temporary lock expiry timestamp

#### Email Verification:
- `is_email_verified` - Email verification status
- `email_verification_token` - Token for email verification
- `email_verification_token_expiry` - Token expiration

#### Profile Information:
- `first_name` - User's first name
- `last_name` - User's last name
- `phone_number` - Contact number

#### Timestamps:
- `created_at` - Account creation date
- `updated_at` - Last modification date (auto-updated)
- `last_login` - Last successful login

#### Constraints:
- Email format validation (regex)
- Username minimum length (3 characters)
- Foreign key to roles table

---

### 3. **EMAIL_VERIFICATION_TOKENS Table**
Manages email verification tokens.

| Column      | Type      | Description                    |
|-------------|-----------|--------------------------------|
| id          | BIGSERIAL | Primary key                    |
| user_id     | BIGINT    | FK to users (CASCADE DELETE)   |
| token       | VARCHAR   | Unique verification token      |
| expiry_date | TIMESTAMP | Token expiration               |
| is_used     | BOOLEAN   | Token usage status             |
| created_at  | TIMESTAMP | Creation timestamp             |

---

### 4. **PASSWORD_RESET_TOKENS Table**
Manages password reset functionality.

| Column      | Type      | Description                    |
|-------------|-----------|--------------------------------|
| id          | BIGSERIAL | Primary key                    |
| user_id     | BIGINT    | FK to users (CASCADE DELETE)   |
| token       | VARCHAR   | Unique reset token             |
| expiry_date | TIMESTAMP | Token expiration               |
| is_used     | BOOLEAN   | Token usage status             |
| created_at  | TIMESTAMP | Creation timestamp             |

---

### 5. **USER_SESSIONS Table**
Tracks active user sessions for security.

| Column        | Type      | Description                    |
|---------------|-----------|--------------------------------|
| id            | BIGSERIAL | Primary key                    |
| user_id       | BIGINT    | FK to users (CASCADE DELETE)   |
| session_token | VARCHAR   | JWT or session token           |
| ip_address    | VARCHAR   | User's IP address              |
| user_agent    | TEXT      | Browser/device info            |
| is_active     | BOOLEAN   | Session active status          |
| created_at    | TIMESTAMP | Session creation               |
| expires_at    | TIMESTAMP | Session expiration             |
| last_accessed | TIMESTAMP | Last activity timestamp        |

---

### 6. **AUDIT_LOGS Table**
Security audit trail for all important actions.

| Column        | Type      | Description                    |
|---------------|-----------|--------------------------------|
| id            | BIGSERIAL | Primary key                    |
| user_id       | BIGINT    | FK to users (SET NULL)         |
| action        | VARCHAR   | Action performed               |
| resource_type | VARCHAR   | Type of resource accessed      |
| resource_id   | VARCHAR   | ID of resource                 |
| ip_address    | VARCHAR   | User's IP address              |
| user_agent    | TEXT      | Browser/device info            |
| details       | JSONB     | Additional JSON details        |
| created_at    | TIMESTAMP | Action timestamp               |

---

## 🔍 Database Views

### v_active_users
Shows all active, non-locked users with their roles.

### v_admin_users
Lists all administrator accounts.

### v_user_statistics
Provides statistics on users by role (total, active, verified, locked).

---

## 🔐 Security Features

### 1. **Role-Based Access Control (RBAC)**
- Two distinct roles: ADMIN and USER
- Role stored as ENUM type for data integrity
- Foreign key relationship to roles table

### 2. **Account Locking**
- Automatic locking after failed login attempts
- Temporary locks with `locked_until` timestamp
- `is_locked` flag for permanent locks

### 3. **Email Verification**
- Separate token management table
- Token expiry validation
- Prevents unauthorized access to unverified accounts

### 4. **Password Reset Security**
- Secure token generation
- Token expiration
- One-time use tokens (is_used flag)

### 5. **Session Management**
- Track all active sessions
- IP address and user agent logging
- Session expiration management

### 6. **Audit Trail**
- Complete action logging
- User activity tracking
- Security incident investigation

### 7. **Data Validation**
- Email format validation (CHECK constraint)
- Username length validation
- Future date validation for expiry fields

---

## 📈 Database Indexes

Performance optimized with indexes on:
- `users.email`
- `users.username`
- `users.role`
- `users.is_active`
- `users.created_at`
- All token tables (user_id, token, expiry_date)
- Session management (user_id, token, is_active)
- Audit logs (user_id, action, created_at)

---

## 🔄 Automatic Triggers

### Update Timestamp Trigger
Automatically updates `updated_at` column on record modification for:
- `users` table
- `roles` table

---

## 👤 Default Admin Account

A default admin account has been created:

**Email:** `admin@prepnow.com`  
**Password:** `Admin@123`

⚠️ **IMPORTANT:** Change this password immediately after first login!

---

## 📝 Migration Summary

### What Was Done:
1. ✅ Created ENUM type `role_type` for ADMIN/USER roles
2. ✅ Created `roles` table with default entries
3. ✅ Enhanced `users` table with:
   - Role management (role, role_id)
   - Security features (locking, failed attempts)
   - Email verification fields
   - Profile information
   - Audit timestamps
4. ✅ Created `email_verification_tokens` table
5. ✅ Created `password_reset_tokens` table
6. ✅ Created `user_sessions` table for session tracking
7. ✅ Created `audit_logs` table for security auditing
8. ✅ Added comprehensive indexes for performance
9. ✅ Created automatic timestamp update triggers
10. ✅ Created utility views for easier querying
11. ✅ Migrated existing users to new schema
12. ✅ Promoted first user to ADMIN role
13. ✅ Created default admin account

### Existing Users:
- All existing users retained in database
- First user promoted to ADMIN role
- All other users assigned USER role
- All accounts marked as active

---

## 🔧 SQL Files Created

1. **`schema_migration_admin_user.sql`**
   - Complete schema definition
   - All tables, indexes, views, triggers
   - Default admin account creation

2. **`migrate_existing_users.sql`**
   - Safe migration of existing user data
   - Adds new columns to existing records
   - Promotes first user to admin

3. **`apply_schema_migration.sh`**
   - Automated migration script
   - Database backup before migration
   - Step-by-step execution
   - Verification queries

---

## 📋 Next Steps

### 1. Update Java Entity Classes
Update the `User.java` entity to match the new schema:
- Add role field (ENUM or String with @Enumerated)
- Add all new security fields
- Add profile fields
- Add timestamp fields

### 2. Update Security Configuration
- Implement role-based authorization (@PreAuthorize)
- Add role checks in controllers
- Implement account locking logic
- Add failed login attempt tracking

### 3. Update Controllers
- Add admin-only endpoints
- Implement role checking
- Add audit logging
- Update authentication responses to include role

### 4. Update Frontend
- Add role-based UI components
- Admin dashboard
- User management interface
- Role-specific navigation

### 5. Security Enhancements
- Implement password reset flow
- Add session management
- Implement audit logging
- Add rate limiting for failed logins

---

## 🧪 Testing Queries

```sql
-- Check user statistics
SELECT * FROM v_user_statistics;

-- View all admin users
SELECT * FROM v_admin_users;

-- View all active users
SELECT * FROM v_active_users;

-- Check specific user role
SELECT username, email, role, is_active FROM users WHERE email = 'your@email.com';

-- View audit logs
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 10;

-- View active sessions
SELECT u.username, s.ip_address, s.created_at, s.expires_at 
FROM user_sessions s 
JOIN users u ON s.user_id = u.id 
WHERE s.is_active = TRUE;
```

---

## 🔒 Security Best Practices

1. **Change default admin password immediately**
2. **Use strong password policies** (minimum length, complexity)
3. **Implement rate limiting** on login endpoints
4. **Enable HTTPS** for all authentication endpoints
5. **Regularly review audit logs** for suspicious activity
6. **Implement session timeout** for inactive users
7. **Use secure token generation** for email verification and password reset
8. **Store passwords using BCrypt** with appropriate salt rounds
9. **Implement CSRF protection**
10. **Add MFA (Multi-Factor Authentication)** for admin accounts

---

## 📞 Support

For any issues or questions regarding the schema migration, refer to:
- Database backup files (created before migration)
- Migration logs
- This documentation

---

**Migration Date:** November 3, 2025  
**Status:** ✅ COMPLETED SUCCESSFULLY

