#!/bin/bash
# SA_Thinking_code — start everything, in the correct dependency order.
# Run this from inside the SA_Thinking_code folder: ./start-all.sh

set -e
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$BASE_DIR/.pids"
LOG_DIR="$BASE_DIR/.logs"
mkdir -p "$PID_DIR" "$LOG_DIR"

REPLICA_DIR="$HOME/postgresql-replica"
PG15_BIN="/opt/homebrew/opt/postgresql@15/bin"

echo "=================================================="
echo " SA_Thinking_code — starting full stack"
echo "=================================================="

echo ""
echo "[1/6] Postgres replica (port 5433)"
if "$PG15_BIN/pg_isready" -p 5433 > /dev/null 2>&1; then
  echo "      already running."
else
  "$PG15_BIN/pg_ctl" -D "$REPLICA_DIR" -l "$REPLICA_DIR/replica.log" start
  sleep 2
fi

echo ""
echo "[2/6] Redis"
if redis-cli ping > /dev/null 2>&1; then
  echo "      already running."
else
  brew services start redis
  sleep 2
fi

echo ""
echo "Note: Postgres primary (5432) is assumed to already be running as a"
echo "background service (Homebrew), same as before this project existed."
echo "This script does not touch it."

start_instance() {
  local name=$1
  local dir=$2
  local port=$3

  cd "$BASE_DIR/$dir"
  nohup mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$port" \
    > "$LOG_DIR/${name}-${port}.log" 2>&1 &
  echo $! > "$PID_DIR/${name}-${port}.pid"
  cd "$BASE_DIR"
  echo "      $name on port $port  (PID $(cat "$PID_DIR/${name}-${port}.pid"), log: .logs/${name}-${port}.log)"
}

echo ""
echo "[3/6] Eureka Server (port 8761)"
start_instance "eureka-server" "eureka-server" 8761
echo "      waiting for Eureka to accept connections..."
until curl -s http://localhost:8761 > /dev/null 2>&1; do sleep 2; done
echo "      Eureka is up."

echo ""
echo "[4/6] Auth Service (port 8083)"
start_instance "auth-service" "auth-service" 8083

echo ""
echo "[5/6] Product Service (3 instances) + Order Service (3 instances)"
start_instance "product-service" "product-service" 8081
start_instance "product-service" "product-service" 8084
start_instance "product-service" "product-service" 8085
start_instance "order-service"   "order-service"   8082
start_instance "order-service"   "order-service"   8086
start_instance "order-service"   "order-service"   8087

echo ""
echo "      waiting ~25s for all instances to register with Eureka..."
sleep 25

echo ""
echo "[6/6] API Gateway (port 8080)"
start_instance "api-gateway" "api-gateway" 8080
sleep 5

echo ""
echo "=================================================="
echo " All services launched."
echo "   Eureka dashboard : http://localhost:8761"
echo "   Gateway entry     : http://localhost:8080"
echo "   Logs              : $LOG_DIR"
echo "   PIDs              : $PID_DIR"
echo "=================================================="
echo ""
echo "Give it 10-15 more seconds, then check the Eureka dashboard to"
echo "confirm PRODUCT-SERVICE, ORDER-SERVICE, AUTH-SERVICE, and API-GATEWAY"
echo "each show the expected number of instances before testing."
