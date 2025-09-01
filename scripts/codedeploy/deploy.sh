#!/bin/bash

if [ $(docker ps -q -f name=$CONTAINER_NAME) ]; then
    echo "Stopping existing container..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
fi

echo "Pulling Docker image: $IMAGE_NAME:$IMAGE_TAG"
docker pull $IMAGE_NAME:$IMAGE_TAG

echo "Running container on port $PORT..."
docker run -d \
  --name $CONTAINER_NAME \
  -p $PORT:8080 \
  $IMAGE_NAME:$IMAGE_TAG

echo "Deployment finished."
docker ps -f name=$CONTAINER_NAME