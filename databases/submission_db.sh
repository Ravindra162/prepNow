#!/bin/bash
set -e

# -----------------------------
# MongoDB Configuration
# -----------------------------
CONTAINER_NAME=submission_cont
VOLUME_NAME=submissionDB
MONGO_IMAGE=mongo:latest
MONGO_INITDB_ROOT_USERNAME=ravindra162
MONGO_INITDB_ROOT_PASSWORD=7065
MONGO_DB=submissiondb
HOST_PORT=27017
CONTAINER_PORT=27017

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
# Run MongoDB container
# -----------------------------
echo "🚀 Starting MongoDB container: $CONTAINER_NAME"
sudo docker run -d \
  --name $CONTAINER_NAME \
  -e MONGO_INITDB_ROOT_USERNAME=$MONGO_INITDB_ROOT_USERNAME \
  -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_INITDB_ROOT_PASSWORD \
  -v $VOLUME_NAME:/data/db \
  -p $HOST_PORT:$CONTAINER_PORT \
  $MONGO_IMAGE

echo "✅ MongoDB container '$CONTAINER_NAME' is up and running!"
echo "   Volume: $VOLUME_NAME"
echo "   Port: $HOST_PORT"
echo "   Root User: $MONGO_INITDB_ROOT_USERNAME"
echo "   Database: $MONGO_DB"
