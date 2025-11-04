-- Check if user exists
SELECT id, email, username, role, is_email_verified FROM users WHERE email = 'tarakravindra242005@gmail.com';

-- Update user to ADMIN role
UPDATE users SET role = 'ADMIN' WHERE email = 'tarakravindra242005@gmail.com';

-- Verify the update
SELECT id, email, username, role, is_email_verified FROM users WHERE email = 'tarakravindra242005@gmail.com';

