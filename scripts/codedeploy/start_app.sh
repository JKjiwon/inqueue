#!/bin/bash

cd /home/ec2-user/inqueue || exit 1

# MySQL 컨테이너가 없으면 시작
if [ -z "$(docker ps -q --filter name=docker-mysql)" ]; then
  echo "Starting MySQL container."
  docker-compose up -d docker-mysql
  echo "Waiting for MySQL to be healthy."
  sleep 20  # healthcheck 기다림
fi

# Redis 컨테이너가 없으면 시작
if [ -z "$(docker ps -q --filter name=docker-redis)" ]; then
  echo "Starting Redis container."
  docker-compose up -d docker-redis
  sleep 5
fi

# 앱 컨테이너 실행
echo "Starting inqueue-service container."
docker-compose up -d --build inqueue-service