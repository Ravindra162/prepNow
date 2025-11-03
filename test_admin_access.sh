#!/bin/bash

# =====================================================
# Quick Start Script for Admin Access Control
# =====================================================
# This script helps you test the new admin-only access control

echo "=========================================="
echo "Admin Access Control - Quick Start"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Check if services are running
echo -e "${BLUE}Step 1: Checking if services are running...${NC}"
if sudo docker ps | grep -q "auth_cont"; then
    echo -e "${GREEN}✓ AuthService container is running${NC}"
else
    echo -e "${RED}✗ AuthService container is not running${NC}"
    echo "Please start services with: ./up.sh"
    exit 1
fi

# Step 2: Verify database schema
echo ""
echo -e "${BLUE}Step 2: Verifying database schema...${NC}"
sudo docker exec auth_cont psql -U ravindra162 -d auth_service -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'role';" | grep -q "role"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database schema updated with role field${NC}"
else
    echo -e "${YELLOW}⚠ Role field not found. Run migration script first.${NC}"
fi

# Step 3: Check for admin user
echo ""
echo -e "${BLUE}Step 3: Checking for admin user...${NC}"
ADMIN_COUNT=$(sudo docker exec auth_cont psql -U ravindra162 -d auth_service -t -c "SELECT COUNT(*) FROM users WHERE role = 'ADMIN';" | tr -d '[:space:]')
if [ "$ADMIN_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $ADMIN_COUNT admin user(s)${NC}"
    echo ""
    echo "Admin users:"
    sudo docker exec auth_cont psql -U ravindra162 -d auth_service -c "SELECT id, username, email, role, is_active FROM users WHERE role = 'ADMIN';"
else
    echo -e "${RED}✗ No admin users found${NC}"
    echo "Creating default admin user..."
    sudo docker exec -i auth_cont psql -U ravindra162 -d auth_service << 'EOF'
INSERT INTO users (
    username,
    email,
    password,
    role,
    is_email_verified,
    is_active
) VALUES (
    'admin',
    'admin@prepnow.com',
    '$2a$10$rN8qT5lZxLxE6xKxC.DkEO7QX5xJ8bKPH8XZYH1nQ7Y6xZ9K5L4Aa',
    'ADMIN',
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;
EOF
    echo -e "${GREEN}✓ Default admin user created${NC}"
fi

# Step 4: Show all users
echo ""
echo -e "${BLUE}Step 4: Current users in the system:${NC}"
sudo docker exec auth_cont psql -U ravindra162 -d auth_service -c "SELECT id, username, email, role, is_email_verified, is_active FROM users ORDER BY role DESC, id;"

# Step 5: Test credentials
echo ""
echo "=========================================="
echo -e "${GREEN}Setup Complete!${NC}"
echo "=========================================="
echo ""
echo -e "${YELLOW}Test Credentials:${NC}"
echo ""
echo "Admin Account:"
echo "  Email:    admin@prepnow.com"
echo "  Password: Admin@123"
echo "  Access:   Full system access (Questions, Assessments, Companies, Settings)"
echo ""
echo "Regular User:"
echo "  Create a new account via signup"
echo "  Access:   Assessment taking and results viewing only"
echo ""
echo -e "${YELLOW}Important URLs:${NC}"
echo "  Frontend:  http://localhost:5173"
echo "  Login:     http://localhost:5173/login"
echo "  Admin:     http://localhost:5173/admin (Admin only)"
echo ""
echo -e "${YELLOW}Testing Steps:${NC}"
echo "1. Login as admin@prepnow.com → Should redirect to /admin"
echo "2. Create a new user account → Should redirect to /dashboard"
echo "3. Try accessing /admin as regular user → Should be blocked"
echo ""
echo -e "${RED}⚠️  IMPORTANT:${NC} Change the default admin password after first login!"
echo ""
echo "For detailed documentation, see:"
echo "  - ADMIN_ACCESS_CONTROL_GUIDE.md"
echo "  - SCHEMA_MIGRATION_DOCUMENTATION.md"
echo ""

