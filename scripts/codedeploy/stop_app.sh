#!/bin/bash

APP_CONTAINER=$(docker ps -q --filter "name=inqueue-service")
if [ -n "$APP_CONTAINER" ]; then
  echo "Stopping inqueue-service container"
  docker stop $APP_CONTAINER
  docker rm $APP_CONTAINER
else
  echo "No inqueue-service container running."
fi