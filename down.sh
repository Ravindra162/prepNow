#!/bin/bash
set -e

# -----------------------------
# PostgreSQL containers
# -----------------------------
postgres_containers=(
  "auth_cont"
  "assessment_cont"
  "questions_cont"
)

for cname in "${postgres_containers[@]}"; do
  if docker ps --format '{{.Names}}' | grep -w $cname >/dev/null; then
    echo "Stopping container $cname"
    docker stop $cname
  else
    echo "Container $cname is not running"
  fi
done

# -----------------------------
# MongoDB container
# -----------------------------
MONGO_CONTAINER=submissionDB

if docker ps --format '{{.Names}}' | grep -w $MONGO_CONTAINER >/dev/null; then
  echo "Stopping MongoDB container $MONGO_CONTAINER"
  docker stop $MONGO_CONTAINER
else
  echo "MongoDB container $MONGO_CONTAINER is not running"
fi

echo "🛑 All database containers have been stopped (volumes preserved)."
