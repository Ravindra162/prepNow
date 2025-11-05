#!/bin/bash
set -e

# -----------------------------
# Configuration (fixed names)
# -----------------------------
declare -A containers=(
  ["auth_cont"]="Auth Database"
  ["assessment_cont"]="Assessment Database"
  ["questions_cont"]="Questions Database"
  ["submission_cont"]="Submission Database (MongoDB)"
  ["notification_cont"]="Notification Database (MongoDB)"
  ["rabbit_mq"]="RabbitMQ Broker"
)

# -----------------------------
# Function to stop and remove containers
# -----------------------------
stop_container() {
  local name=$1
  local display_name=$2

  if sudo docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
    echo "🛑 Stopping $display_name..."
    sudo docker stop "$name" >/dev/null 2>&1 || true
    echo "🧹 Removing $display_name..."
    sudo docker rm "$name" >/dev/null 2>&1 || true
    echo "✅ $display_name removed successfully!"
  else
    echo "⚠️  $display_name container not found — skipping."
  fi
}

# -----------------------------
# Stop all containers
# -----------------------------
echo "===================================="
echo "🧩 Stopping and removing all database & RabbitMQ containers..."
echo "===================================="

for name in "${!containers[@]}"; do
  stop_container "$name" "${containers[$name]}"
done

# -----------------------------
# Remove volumes (optional)
# -----------------------------


echo "===================================="
echo "✅ All containers stopped and cleaned up!"
echo "===================================="

# -----------------------------
# Show remaining containers
# -----------------------------
echo
echo "📋 Remaining containers:"
sudo docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
