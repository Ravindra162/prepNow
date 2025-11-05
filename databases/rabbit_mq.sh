#!/bin/bash
set -e

# -----------------------------
# RabbitMQ Configuration
# -----------------------------
CONTAINER_NAME=rabbit_mq
VOLUME_NAME=rabbitmq_data
NETWORK_NAME=prepnow-network
RABBITMQ_IMAGE=rabbitmq:3-management
HOST_PORT_AMQP=5672
HOST_PORT_UI=15672
DEFAULT_USER=guest
DEFAULT_PASS=guest

# -----------------------------
# Create Docker network if it doesn't exist
# -----------------------------
if [ -z "$(sudo docker network ls -q -f name=$NETWORK_NAME)" ]; then
  echo "🔧 Creating Docker network: $NETWORK_NAME"
  sudo docker network create $NETWORK_NAME
else
  echo "✅ Docker network $NETWORK_NAME already exists"
fi

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
# Run RabbitMQ container
# -----------------------------
echo "🚀 Starting RabbitMQ container: $CONTAINER_NAME"
sudo docker run -d \
  --name $CONTAINER_NAME \
  --network $NETWORK_NAME \
  -p $HOST_PORT_AMQP:5672 \
  -p $HOST_PORT_UI:15672 \
  -e RABBITMQ_DEFAULT_USER=$DEFAULT_USER \
  -e RABBITMQ_DEFAULT_PASS=$DEFAULT_PASS \
  -v $VOLUME_NAME:/var/lib/rabbitmq \
  $RABBITMQ_IMAGE

echo "✅ RabbitMQ container '$CONTAINER_NAME' is up and running!"
echo "   Management UI: http://localhost:$HOST_PORT_UI"
echo "   Username: $DEFAULT_USER"
echo "   Password: $DEFAULT_PASS"
