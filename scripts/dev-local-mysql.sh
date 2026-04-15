#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

if [[ -f "${PROJECT_ROOT}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${PROJECT_ROOT}/.env"
  set +a
fi

cd "${PROJECT_ROOT}"

"${PROJECT_ROOT}/scripts/start-docker.sh"

is_port_in_use() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1
    return $?
  fi

  if command -v nc >/dev/null 2>&1; then
    nc -z 127.0.0.1 "${port}" >/dev/null 2>&1
    return $?
  fi

  return 1
}

pick_server_port() {
  local preferred_port="$1"
  local candidate_port="${preferred_port}"

  while is_port_in_use "${candidate_port}"; do
    candidate_port="$((candidate_port + 1))"
  done

  echo "${candidate_port}"
}

DEFAULT_SERVER_PORT="${SERVER_PORT:-10002}"
LOCAL_SERVER_PORT="${LOCAL_SERVER_PORT:-$(pick_server_port "${DEFAULT_SERVER_PORT}")}"
LOCAL_CONTEXT_PATH="${LOCAL_CONTEXT_PATH:-${SERVER_SERVLET_CONTEXT_PATH:-/eftboard-server}}"
LOCAL_MYSQL_HOST="${LOCAL_MYSQL_HOST:-127.0.0.1}"
LOCAL_MYSQL_PORT="${LOCAL_MYSQL_PORT:-${MYSQL_PORT:-3306}}"
LOCAL_MYSQL_DATABASE="${LOCAL_MYSQL_DATABASE:-${MYSQL_DATABASE:-tarkov_board}}"
LOCAL_MYSQL_USERNAME="${LOCAL_MYSQL_USERNAME:-${SPRING_DATASOURCE_USERNAME:-root}}"
LOCAL_MYSQL_PASSWORD="${LOCAL_MYSQL_PASSWORD:-${SPRING_DATASOURCE_PASSWORD:-${MYSQL_ROOT_PASSWORD:-Peichun@92755}}}"

echo
echo "Starting backend for local MySQL debugging..."
echo "- Backend: http://127.0.0.1:${LOCAL_SERVER_PORT}${LOCAL_CONTEXT_PATH}"
echo "- MySQL: ${LOCAL_MYSQL_HOST}:${LOCAL_MYSQL_PORT}/${LOCAL_MYSQL_DATABASE}"
echo "- Username: ${LOCAL_MYSQL_USERNAME}"
if [[ "${LOCAL_SERVER_PORT}" != "${DEFAULT_SERVER_PORT}" ]]; then
  echo "- Notice: preferred port ${DEFAULT_SERVER_PORT} was busy, switched to ${LOCAL_SERVER_PORT}"
fi
echo

SERVER_PORT="${LOCAL_SERVER_PORT}" \
SERVER_SERVLET_CONTEXT_PATH="${LOCAL_CONTEXT_PATH}" \
SPRING_DATASOURCE_URL="jdbc:mysql://${LOCAL_MYSQL_HOST}:${LOCAL_MYSQL_PORT}/${LOCAL_MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" \
SPRING_DATASOURCE_USERNAME="${LOCAL_MYSQL_USERNAME}" \
SPRING_DATASOURCE_PASSWORD="${LOCAL_MYSQL_PASSWORD}" \
mvn spring-boot:run
