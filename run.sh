#!/usr/bin/env bash
# run.sh - start all services in this repo (java services + frontend)
# Usage: ./run.sh    or set environment variables to override directories, e.g.
#   ASSESSMENT_DIR=AssessmentService AUTH_DIR=AuthService ./run.sh

set -euo pipefail

# Default directories (override with env vars if needed)
ASSESSMENT_DIR="${ASSESSMENT_DIR:-AssessmentService}"
AUTH_DIR="${AUTH_DIR:-AuthService}"
QUESTION_DIR="${QUESTION_DIR:-QuestionService}"
SUBMISSION_DIR="${SUBMISSION_DIR:-SubmissionService}"
FRONTEND_DIR="${FRONTEND_DIR:-frontend}"
LOG_DIR="${LOG_DIR:-logs}"

mkdir -p "$LOG_DIR"

info(){ printf "\n[INFO] %s\n" "$1"; }
err(){ printf "\n[ERROR] %s\n" "$1" >&2; }

PIDS=()
SERVICES_STARTED=()

start_java_service() {
  local dir="$1"
  local name="$2"

  if [ ! -d "$dir" ]; then
    info "Directory \`$dir\` not found, skipping $name."
    return
  fi

  pushd "$dir" > /dev/null

  # find any existing jar (newest non-original jar)
  local existing_jar
  existing_jar=$(ls -t target/*jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)

  # Prefer mvnw, then mvn; if neither, we'll try to use an existing jar
  if [ -x ./mvnw ]; then
    MVN_CMD=("./mvnw")
  elif command -v mvn >/dev/null 2>&1; then
    MVN_CMD=("mvn")
  else
    MVN_CMD=()
  fi

  info "Preparing $name from \`$dir\`..."

  # If we have a jar already and FORCE_BUILD is not set, skip build
  if [ -n "$existing_jar" ] && [ -z "${FORCE_BUILD:-}" ]; then
    info "Found existing jar for $name: $existing_jar (skipping build)."
    JAR_PATH="$existing_jar"
    info "Starting $name using jar: $JAR_PATH"
    nohup java -jar "$JAR_PATH" > "../$LOG_DIR/${name}.log" 2>&1 &
    local pid=$!
  else
    if [ ${#MVN_CMD[@]} -gt 0 ]; then
      info "Building $name with \`${MVN_CMD[*]} -DskipTests package\`..."
      if ! "${MVN_CMD[@]}" -DskipTests package -q; then
        err "Build failed for $name. Check the maven output above. Skipping start."
        popd > /dev/null
        return
      fi
      # find the built jar (newest non-original jar)
      JAR_PATH=$(ls -t target/*jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)
      if [ -z "$JAR_PATH" ]; then
        err "Build succeeded but no jar found for $name in target/. Skipping."
        popd > /dev/null
        return
      fi
      info "Starting $name using jar: $JAR_PATH"
      nohup java -jar "$JAR_PATH" > "../$LOG_DIR/${name}.log" 2>&1 &
      local pid=$!
    else
      # No maven available; fallback to existing jar in target/
      if [ -n "$existing_jar" ]; then
        info "No mvn/mvnw available. Starting $name using existing jar: $existing_jar"
        nohup java -jar "$existing_jar" > "../$LOG_DIR/${name}.log" 2>&1 &
        local pid=$!
      else
        err "No mvnw/mvn and no built jar found for $name in $dir. Skipping."
        popd > /dev/null
        return
      fi
    fi
  fi

  PIDS+=("$pid")
  SERVICES_STARTED+=("$name")
  popd > /dev/null
  info "$name started (PID: $pid). Logs: \`$LOG_DIR/${name}.log\`"
}

start_frontend() {
  local dir="$1"
  local name="$2"

  if [ ! -d "$dir" ]; then
    info "Directory \`$dir\` not found, skipping $name."
    return
  fi

  if ! command -v npm >/dev/null 2>&1; then
    err "npm not found. Skipping frontend start."
    return
  fi

  pushd "$dir" > /dev/null
  # Install deps if needed; capture install logs to aid debugging
  INSTALL_LOG="../$LOG_DIR/${name}-npm-install.log"
  if [ ! -d node_modules ]; then
    info "Installing frontend dependencies in \`$dir\` (npm ci)... Logs: $INSTALL_LOG"
    if npm ci > "$INSTALL_LOG" 2>&1; then
      info "npm ci succeeded"
    else
      info "npm ci failed; trying npm install and preserving logs"
      if npm install > "$INSTALL_LOG" 2>&1; then
        info "npm install succeeded"
      else
        err "Frontend dependency install failed. See $INSTALL_LOG for details (last 50 lines):"
        tail -n 50 "$INSTALL_LOG" | sed 's/^/    /'
        popd > /dev/null
        return
      fi
    fi
  fi

  # Use a lightweight package.json check for a "dev" script to avoid invoking `npm run`
  if [ -f package.json ] && grep -q '"dev"' package.json; then
    CMD=(npm run dev --silent)
  else
    CMD=(npm run start --silent)
  fi

  info "Starting frontend from \`$dir\`..."
  nohup ${CMD[@]} > "../$LOG_DIR/${name}.log" 2>&1 &
  local pid=$!
  PIDS+=("$pid")
  SERVICES_STARTED+=("$name")
  popd > /dev/null
  info "$name started (PID: $pid). Logs: \`$LOG_DIR/${name}.log\`"
}

stop_all() {
  info "Stopping services: ${SERVICES_STARTED[*]:-none}"
  for pid in "${PIDS[@]:-}"; do
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
  done

  # Give processes a moment to exit
  sleep 1
  for pid in "${PIDS[@]:-}"; do
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill -9 "$pid" 2>/dev/null || true
    fi
  done

  wait 2>/dev/null || true
  info "Stopped."
}

trap stop_all EXIT INT TERM

# Start services (names are used for log filenames)
start_java_service "$ASSESSMENT_DIR" "AssessmentService"
start_java_service "$AUTH_DIR" "AuthService"
start_java_service "$QUESTION_DIR" "QuestionService"
start_java_service "$SUBMISSION_DIR" "SubmissionService"
start_frontend "$FRONTEND_DIR" "frontend"

info "All requested services attempted to start. Logs: \`$LOG_DIR/*.log\`"
info "Press Ctrl+C to stop all services."

# Keep script running so trap can catch Ctrl+C and exit.
while true; do sleep 3600; done
