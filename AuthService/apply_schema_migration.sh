#!/bin/bash

# =====================================================
# Schema Migration Application Script
# =====================================================
# This script applies the database schema migration
# for admin and user separation
# =====================================================

set -e  # Exit on error

CONTAINER_NAME="auth_cont"
DB_NAME="auth_service"
DB_USER="ravindra162"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "Auth Service Schema Migration"
echo "=========================================="
echo ""

# Check if container is running
if ! sudo docker ps | grep -q $CONTAINER_NAME; then
    echo "Error: Container $CONTAINER_NAME is not running!"
    exit 1
fi

echo "Step 1: Backing up current database..."
sudo docker exec $CONTAINER_NAME pg_dump -U $DB_USER $DB_NAME > "$SCRIPT_DIR/backup_$(date +%Y%m%d_%H%M%S).sql"
echo "✓ Backup completed"
echo ""

echo "Step 2: Applying main schema migration..."
sudo docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPT_DIR/schema_migration_admin_user.sql"
echo "✓ Schema migration completed"
echo ""

echo "Step 3: Migrating existing user data..."
sudo docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < "$SCRIPT_DIR/migrate_existing_users.sql"
echo "✓ Data migration completed"
echo ""

echo "Step 4: Verifying migration..."
sudo docker exec $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -c "SELECT * FROM v_user_statistics;"
echo ""

echo "Step 5: Showing admin users..."
sudo docker exec $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -c "SELECT username, email, role, is_email_verified, created_at FROM users WHERE role = 'ADMIN';"
echo ""

echo "=========================================="
echo "Migration completed successfully!"
echo "=========================================="
echo ""
echo "Default Admin Credentials:"
echo "  Email: admin@prepnow.com"
echo "  Password: Admin@123"
echo ""
echo "⚠️  IMPORTANT: Change the admin password after first login!"
echo ""

