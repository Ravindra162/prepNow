#!/bin/bash
set -e

# -----------------------------
# PostgreSQL configuration
# -----------------------------
CONTAINER_NAME=auth_cont
VOLUME_NAME=auth_data
POSTGRES_IMAGE=postgres:16
POSTGRES_DB=auth_service
POSTGRES_USER=ravindra162
POSTGRES_PASSWORD=7065
HOST_PORT=5435
CONTAINER_PORT=5432

# -----------------------------
# Create volume if it doesn't exist
# -----------------------------
if [ -z "$(sudo docker volume ls -q -f name=$VOLUME_NAME)" ]; then
  echo "🔧 Creating persistent volume: $VOLUME_NAME"
  sudo docker volume create $VOLUME_NAME
else
  echo "✅ Volume $VOLUME_NAME already exists"
fi

# -----------------------------
# Stop and remove old container if running
# -----------------------------
if [ "$(sudo docker ps -aq -f name=$CONTAINER_NAME)" ]; then
  echo "🛑 Removing existing container: $CONTAINER_NAME"
  sudo docker rm -f $CONTAINER_NAME >/dev/null 2>&1 || true
fi

# -----------------------------
# Run PostgreSQL container
# -----------------------------
echo "🚀 Starting PostgreSQL container: $CONTAINER_NAME"
sudo docker run -d \
  --name $CONTAINER_NAME \
  -e POSTGRES_USER=$POSTGRES_USER \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  -e POSTGRES_DB=$POSTGRES_DB \
  -p $HOST_PORT:$CONTAINER_PORT \
  -v $VOLUME_NAME:/var/lib/postgresql/data \
  $POSTGRES_IMAGE

echo "✅ PostgreSQL container '$CONTAINER_NAME' is up and running!"
echo "   Volume: $VOLUME_NAME"
echo "   Port: $HOST_PORT"
echo "   Database: $POSTGRES_DB"
