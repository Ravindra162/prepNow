#!/bin/bash
set -e

# -----------------------------
# Configuration
# -----------------------------
declare -A services=(
  ["auth_db.sh"]="Auth Database"
  ["assessment_db.sh"]="Assessment Database"
  ["questions_db.sh"]="Questions Database"
  ["submission_db.sh"]="Submission Database (MongoDB)"
  ["notification_db.sh"]="Notification Database (MongoDB)"
  ["rabbitmq.sh"]="RabbitMQ Broker"
)

# -----------------------------
# Function to start services
# -----------------------------
start_service() {
  local script=$1
  local name=$2

  if [ -x "./$script" ]; then
    echo "🚀 Starting $name..."
    ./"$script" >/dev/null 2>&1
    echo "✅ $name started successfully!"
  else
    echo "⚠️  Skipping $name — script $script not found or not executable."
  fi
}

# -----------------------------
# Start all services
# -----------------------------
echo "===================================="
echo "🧩 Starting all database containers..."
echo "===================================="

for script in "${!services[@]}"; do
  start_service "$script" "${services[$script]}"
done

echo "===================================="
echo "✅ All database and RabbitMQ containers are up and running!"
echo "===================================="

# -----------------------------
# Display container status
# -----------------------------
echo
echo "📋 Current running containers:"
sudo docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
