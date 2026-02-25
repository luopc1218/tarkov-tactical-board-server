#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker/docker-compose.dev.yml"

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-Peichun@92755}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker not found. Please install Docker Desktop first."
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "Error: docker compose not found."
  exit 1
fi

echo "Starting MySQL and MinIO containers..."
"${COMPOSE_CMD[@]}" -f "${COMPOSE_FILE}" up -d

MYSQL_CONTAINER="$("${COMPOSE_CMD[@]}" -f "${COMPOSE_FILE}" ps -q mysql)"
if [[ -z "${MYSQL_CONTAINER}" ]]; then
  echo "Error: mysql container was not created."
  exit 1
fi

echo "Waiting for MySQL to become ready..."
for i in {1..60}; do
  if docker exec "${MYSQL_CONTAINER}" mysqladmin ping -h 127.0.0.1 -uroot "-p${MYSQL_ROOT_PASSWORD}" --silent >/dev/null 2>&1; then
    break
  fi
  if [[ "${i}" -eq 60 ]]; then
    echo "Error: MySQL did not become ready in time."
    exit 1
  fi
  sleep 2
done

echo "Initializing database (tarkov_board) ..."
docker exec "${MYSQL_CONTAINER}" mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -e \
  "CREATE DATABASE IF NOT EXISTS tarkov_board DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo
echo "Docker services are ready:"
echo "- MySQL: 127.0.0.1:3306 (root password from MYSQL_ROOT_PASSWORD, default: Peichun@92755)"
echo "- MinIO API: http://localhost:9000"
echo "- MinIO Console: http://localhost:9001 (default: minioadmin / minioadmin)"
echo
echo "Then run backend with: mvn spring-boot:run"
