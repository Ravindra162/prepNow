#!/bin/bash

echo "================================================"
echo "Building NotificationService with all dependencies"
echo "================================================"

cd /home/ravindra162/Desktop/prepNow/NotificationService

echo ""
echo "Step 1: Cleaning previous builds..."
mvn clean

echo ""
echo "Step 2: Downloading dependencies..."
mvn dependency:resolve

echo ""
echo "Step 3: Compiling project..."
mvn compile

echo ""
echo "Step 4: Installing to local repository..."
mvn install -DskipTests

echo ""
echo "================================================"
echo "Build Complete!"
echo "================================================"
echo ""
echo "If you're still seeing errors in your IDE:"
echo "1. In IntelliJ IDEA: Right-click on pom.xml → Maven → Reload Project"
echo "2. Or use: File → Invalidate Caches → Restart"
echo ""

