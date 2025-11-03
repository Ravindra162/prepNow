-- =====================================================
-- Migration Script for Existing Users
-- =====================================================
-- This script safely migrates existing user data to the new schema
-- Run this AFTER the main schema migration
-- =====================================================

-- Step 1: Add new columns to existing users table if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

-- Step 2: Set role_id for existing users
UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'USER')
WHERE role_id IS NULL AND role = 'USER';

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE role_id IS NULL AND role = 'ADMIN';

-- Step 3: Set default values for new columns on existing records
UPDATE users
SET
    is_active = TRUE,
    is_locked = FALSE,
    failed_login_attempts = 0
WHERE is_active IS NULL;

-- Step 4: Add foreign key constraint if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_users_role_id' AND table_name = 'users'
    ) THEN
        ALTER TABLE users
        ADD CONSTRAINT fk_users_role_id
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Step 5: Mark first user as admin if no admin exists
DO $$
DECLARE
    admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO admin_count FROM users WHERE role = 'ADMIN';

    IF admin_count = 0 THEN
        UPDATE users
        SET
            role = 'ADMIN',
            role_id = (SELECT id FROM roles WHERE name = 'ADMIN'),
            is_email_verified = TRUE
        WHERE id = (SELECT MIN(id) FROM users);

        RAISE NOTICE 'First user has been promoted to ADMIN';
    END IF;
END $$;

-- Step 6: Show migration results
SELECT
    'Migration Summary' as info,
    COUNT(*) as total_users,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admin_users,
    COUNT(CASE WHEN role = 'USER' THEN 1 END) as regular_users
FROM users;

COMMIT;

