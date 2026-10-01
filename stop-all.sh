#!/bin/bash
# SA_Thinking_code — stop everything started by start-all.sh.
# Run this from inside the SA_Thinking_code folder: ./stop-all.sh

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$BASE_DIR/.pids"
REPLICA_DIR="$HOME/postgresql-replica"
PG15_BIN="/opt/homebrew/opt/postgresql@15/bin"

echo "=================================================="
echo " SA_Thinking_code — stopping full stack"
echo "=================================================="

if [ -d "$PID_DIR" ] && [ -n "$(ls -A "$PID_DIR" 2>/dev/null)" ]; then
  echo ""
  echo "[1/2] Stopping application services..."
  for pidfile in "$PID_DIR"/*.pid; do
    [ -e "$pidfile" ] || continue
    pid=$(cat "$pidfile")
    name=$(basename "$pidfile" .pid)

    if ps -p "$pid" > /dev/null 2>&1; then
      echo "      stopping $name (PID $pid)..."
      # mvn spring-boot:run forks a child JVM — kill children first, then the parent.
      pkill -P "$pid" 2>/dev/null
      kill "$pid" 2>/dev/null
    else
      echo "      $name (PID $pid) already not running."
    fi
    rm -f "$pidfile"
  done

  echo "      waiting for processes to exit..."
  sleep 6

  # Safety net: anything still holding these ports gets force-killed.
  for port in 8080 8081 8082 8083 8084 8085 8086 8087 8761; do
    pid_on_port=$(lsof -ti :"$port" 2>/dev/null)
    if [ -n "$pid_on_port" ]; then
      echo "      port $port still held by PID $pid_on_port — force killing."
      kill -9 "$pid_on_port" 2>/dev/null
    fi
  done
else
  echo ""
  echo "[1/2] No PID files found — nothing appears to have been started via start-all.sh."
fi

echo ""
echo "[2/2] Stopping Postgres replica (port 5433)..."
"$PG15_BIN/pg_ctl" -D "$REPLICA_DIR" stop -m fast 2>/dev/null || echo "      already stopped."

echo ""
echo "=================================================="
echo " Stopped. Left running on purpose (shared infrastructure):"
echo "   - Postgres primary (port 5432)"
echo "   - Redis"
echo " Stop those yourself if you want a fully clean machine,"
echo " e.g. if you're also done with resilient-catalog for the day."
echo "=================================================="
