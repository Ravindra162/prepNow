#!/bin/bash
set -e

# -----------------------------
# PostgreSQL configuration
# -----------------------------
POSTGRES_IMAGE=postgres:16
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

postgres_containers=(
  "auth_cont:5435:auth_data:auth_db"
  "assessment_cont:5433:assessment_db:assessment_db"
  "questions_cont:5434:questions_db:questions_db"
)

for entry in "${postgres_containers[@]}"; do
  cname=$(echo $entry | cut -d':' -f1)
  port=$(echo $entry | cut -d':' -f2)
  vol=$(echo $entry | cut -d':' -f3)
  dbname=$(echo $entry | cut -d':' -f4)

  # Create volume if missing
  if ! docker volume ls -q | grep -w $vol >/dev/null; then
    echo "Creating volume $vol"
    docker volume create $vol
  fi

  # Start or create container
  if docker ps -a --format '{{.Names}}' | grep -w $cname >/dev/null; then
    echo "Starting existing container $cname"
    docker start $cname
  else
    echo "Running new container $cname (port $port, volume $vol)"
    docker run -d \
      --name $cname \
      -p $port:5432 \
      -v $vol:/var/lib/postgresql/data \
      -e POSTGRES_USER=$POSTGRES_USER \
      -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
      -e POSTGRES_DB=$dbname \
      --restart unless-stopped \
      $POSTGRES_IMAGE
  fi
done

# -----------------------------
# MongoDB configuration
# -----------------------------
MONGO_IMAGE=mongo:latest
MONGO_CONTAINER=submissionDB
MONGO_PORT=27017
MONGO_VOLUME=submission_data
MONGO_USER=admin
MONGO_PASSWORD=admin123

# Create volume if missing
if ! docker volume ls -q | grep -w $MONGO_VOLUME >/dev/null; then
  echo "Creating MongoDB volume $MONGO_VOLUME"
  docker volume create $MONGO_VOLUME
fi

# Start or create MongoDB container
if docker ps -a --format '{{.Names}}' | grep -w $MONGO_CONTAINER >/dev/null; then
  echo "Starting existing MongoDB container $MONGO_CONTAINER"
  docker start $MONGO_CONTAINER
else
  echo "Running new MongoDB container $MONGO_CONTAINER (port $MONGO_PORT, volume $MONGO_VOLUME)"
  docker run -d \
    --name $MONGO_CONTAINER \
    -p $MONGO_PORT:27017 \
    -v $MONGO_VOLUME:/data/db \
    -e MONGO_INITDB_ROOT_USERNAME=$MONGO_USER \
    -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_PASSWORD \
    --restart unless-stopped \
    $MONGO_IMAGE
fi

echo "✅ All database containers are up and running!"
