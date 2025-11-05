-- Notification Service Database Initialization Script
-- This script will create the schema, tables, indexes, and sample data

-- Drop existing schema and all objects (WARNING: This will delete all data!)
DROP SCHEMA IF EXISTS notification_schema CASCADE;

-- Create schema for Notification Service
CREATE SCHEMA notification_schema;

-- Set search path to the new schema
SET search_path TO notification_schema;

-- Create notification_templates table
CREATE TABLE notification_templates (
    template_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    subject_template TEXT NOT NULL,
    body_template TEXT NOT NULL,
    type VARCHAR(100) NOT NULL, -- EMAIL, SMS, PUSH, etc.
    is_active BOOLEAN DEFAULT true,
    variables JSONB, -- JSON array of required template variables
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_template_name_type UNIQUE (name, type)
);

-- Create notifications table
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id BIGINT,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),
    recipient_user_id BIGINT,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- EMAIL, SMS, PUSH, etc.
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED, DELIVERED, READ
    priority INT DEFAULT 3, -- 1: High, 2: Medium, 3: Low
    scheduled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    metadata JSONB, -- Additional metadata like click tracking, open rates, etc.
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES notification_templates(template_id) ON DELETE SET NULL
);

-- Create notification_attachments table
CREATE TABLE notification_attachments (
    attachment_id BIGSERIAL PRIMARY KEY,
    notification_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE
);

-- Create notification_events table for tracking notification interactions
CREATE TABLE notification_events (
    event_id BIGSERIAL PRIMARY KEY,
    notification_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- SENT, DELIVERED, OPENED, CLICKED, BOUNCED, etc.
    event_data JSONB, -- Additional event-specific data
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE
);

-- Create notification_preferences table
CREATE TABLE notification_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    email_frequency VARCHAR(50) DEFAULT 'IMMEDIATE', -- IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_preferences UNIQUE (user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_recipient_user_id ON notifications(recipient_user_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notification_events_notification_id ON notification_events(notification_id);
CREATE INDEX idx_notification_events_created_at ON notification_events(created_at);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);

-- Create function to update the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to update updated_at on all tables
CREATE TRIGGER update_notification_templates_updated_at
BEFORE UPDATE ON notification_templates
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at
BEFORE UPDATE ON notifications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_preferences_updated_at
BEFORE UPDATE ON notification_preferences
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert default notification templates
INSERT INTO notification_templates (
    name,
    description,
    subject_template,
    body_template,
    type,
    is_active,
    variables
) VALUES 
('WELCOME_EMAIL', 'Welcome email for new users', 'Welcome to PrepNow, {{user.firstName}}!', 
'<p>Hello {{user.firstName}},</p><p>Welcome to PrepNow! We''re excited to have you on board.</p>', 
'EMAIL', true, '{"required": ["user.firstName"]}'),

('ASSESSMENT_INVITATION', 'Invitation to take an assessment', 'You''ve been invited to take an assessment: {{assessment.name}}', 
'<p>Hello {{user.firstName}},</p><p>You have been invited to take the assessment: <strong>{{assessment.name}}</strong>.</p>', 
'EMAIL', true, '{"required": ["user.firstName", "assessment.name"]}'),

('PASSWORD_RESET', 'Password reset request', 'Reset your PrepNow password', 
'<p>Hello,</p><p>We received a request to reset your password. Click the link below to reset it:</p><p><a href="{{resetLink}}">Reset Password</a></p>', 
'EMAIL', true, '{"required": ["resetLink"]}'),

('ASSESSMENT_REMINDER', 'Reminder about upcoming assessment', 'Reminder: {{assessment.name}} is due soon', 
'<p>Hello {{user.firstName}},</p><p>This is a reminder that your assessment <strong>{{assessment.name}}</strong> is due on {{assessment.dueDate}}.</p>', 
'EMAIL', true, '{"required": ["user.firstName", "assessment.name", "assessment.dueDate"]}'),

('ASSESSMENT_COMPLETED', 'Confirmation of assessment completion', 'Your assessment has been submitted', 
'<p>Hello {{user.firstName}},</p><p>Thank you for completing the assessment: <strong>{{assessment.name}}</strong>.</p>', 
'EMAIL', true, '{"required": ["user.firstName", "assessment.name"]}');

-- Insert sample notification preferences
INSERT INTO notification_preferences (
    user_id,
    email_enabled,
    sms_enabled,
    push_enabled,
    email_frequency
) VALUES 
(1001, true, true, true, 'IMMEDIATE'),
(1002, true, false, true, 'DAILY_DIGEST'),
(1003, false, true, false, 'WEEKLY_DIGEST');

-- Insert sample notification
WITH template AS (
    SELECT template_id FROM notification_templates WHERE name = 'WELCOME_EMAIL' LIMIT 1
)
INSERT INTO notifications (
    template_id,
    recipient_email,
    recipient_user_id,
    subject,
    body,
    type,
    status,
    priority,
    sent_at,
    delivered_at,
    metadata
)
SELECT 
    template_id,
    'user@example.com',
    1001,
    'Welcome to PrepNow, John!',
    '<p>Hello John,</p><p>Welcome to PrepNow! We''re excited to have you on board.</p>',
    'EMAIL',
    'DELIVERED',
    3,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '55 minutes',
    '{"opened": true, "openedAt": "' || (CURRENT_TIMESTAMP - INTERVAL '50 minutes')::text || '", "device": "desktop"}'
FROM template;

-- Insert notification event for the sample notification
WITH notification AS (
    SELECT notification_id FROM notifications WHERE recipient_email = 'user@example.com' LIMIT 1
)
INSERT INTO notification_events (
    notification_id,
    event_type,
    event_data
)
SELECT 
    notification_id,
    'SENT',
    '{"sentAt": "' || (CURRENT_TIMESTAMP - INTERVAL '1 hour')::text || '", "service": "AWS SES"}'
FROM notification;

-- Create a read-only user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'notification_ro') THEN
        CREATE ROLE notification_ro LOGIN PASSWORD 'readonlypass';
    END IF;
    
    GRANT USAGE ON SCHEMA notification_schema TO notification_ro;
    GRANT SELECT ON ALL TABLES IN SCHEMA notification_schema TO notification_ro;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA notification_schema 
    GRANT SELECT ON TABLES TO notification_ro;
END $$;

-- Create a read-write user for the application
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'notification_rw') THEN
        CREATE ROLE notification_rw LOGIN PASSWORD 'readwritepass';
    END IF;
    
    GRANT USAGE, CREATE ON SCHEMA notification_schema TO notification_rw;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA notification_schema TO notification_rw;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA notification_schema TO notification_rw;
    
    -- Set default privileges for future tables
    ALTER DEFAULT PRIVILEGES IN SCHEMA notification_schema 
    GRANT ALL PRIVILEGES ON TABLES TO notification_rw;
    
    ALTER DEFAULT PRIVILEGES IN SCHEMA notification_schema
    GRANT ALL PRIVILEGES ON SEQUENCES TO notification_rw;
END $$;

-- Print completion message
\echo '\nNotification Service database initialization completed successfully!\n';
\echo 'Sample data has been loaded:';
\echo '  - 5 notification templates (WELCOME_EMAIL, ASSESSMENT_INVITATION, etc.)';
\echo '  - 3 notification preferences';
\echo '  - 1 sample notification with event log\n';
\echo 'Database users created:';
\echo '  - notification_ro (read-only access)';
\echo '  - notification_rw (read-write access)\n';
\echo 'To connect to the database:';
\echo '  psql -h localhost -U notification_rw -d your_database_name';
\echo '  or';
\echo '  psql -h localhost -U notification_ro -d your_database_name\n';
