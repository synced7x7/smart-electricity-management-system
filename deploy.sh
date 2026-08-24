#!/bin/zsh
# Starts the whole DESCO system locally against Supabase:
#
#   api-gateway (8080)  auth (8081)  user (8082)  outage (8083)
#   notification (8084) complaint (8085)  payment (8086)  admin (8087)
#
# The frontend needs ALL of them: the customer dashboard reads outages,
# notifications and complaints, and the admin health panel probes all six
# siblings. Starting only the four owned services leaves half the app dead.
#
# Usage:
#   ./deploy.sh           start everything (builds any missing jar)
#   ./deploy.sh stop      stop everything
#   ./deploy.sh status    show what is up
#   ./deploy.sh restart   stop then start
#   ./deploy.sh build     force a clean rebuild of every jar, then start

ROOT="${0:A:h}"
LOGDIR="$ROOT/.logs"

# name:port, in start order. The gateway is first so it is ready to route by
# the time the services behind it finish booting.
SERVICES=(
  api-gateway:8080
  auth-service:8081
  user-service:8082
  outage-service:8083
  notification-service:8084
  complaint-service:8085
  payment-service:8086
  admin-service:8087
)

kill_port() {
  local pids
  pids=$(lsof -ti:"$1" 2>/dev/null)
  if [[ -n "$pids" ]]; then
    echo "$pids" | xargs kill -9 2>/dev/null
    echo "  stopped process on :$1"
  fi
}

stop_all() {
  for svc in $SERVICES; do kill_port "${svc##*:}"; done
}

health() {
  # curl already prints 000 on a refused connection AND exits non-zero, so a
  # `|| echo 000` fallback appended a second one and produced "000000".
  local out
  out="$(curl -s -o /dev/null -m 2 -w '%{http_code}' "http://localhost:$1/actuator/health" 2>/dev/null)"
  [[ -n "$out" ]] && echo "$out" || echo 000
}

show_status() {
  local up=0
  for svc in $SERVICES; do
    local name="${svc%%:*}"
    local port="${svc##*:}"
    # Assign on the same line: a bare `local code` makes zsh print the
    # parameter when it already holds a value from a previous iteration.
    local code="$(health "$port")"
    if [[ "$code" == "200" ]]; then
      printf '  \033[32m●\033[0m %-22s :%s\n' "$name" "$port"
      ((up++))
    else
      printf '  \033[31m○\033[0m %-22s :%s  (%s)\n' "$name" "$port" "$code"
    fi
  done
  echo "  $up/${#SERVICES[@]} up"
}

case "$1" in
  stop)
    stop_all
    echo "done"
    exit 0
    ;;
  status)
    show_status
    exit 0
    ;;
esac

if [[ ! -f "$ROOT/.env" ]]; then
  echo "ERROR: $ROOT/.env not found (copy .env.example and fill in the Supabase credentials)"
  exit 1
fi

set -a
source "$ROOT/.env"
set +a

# The gateway's route URLs default to docker-compose hostnames
# (http://auth-service:8081), which do not resolve outside a compose network.
# All seven must be overridden — missing even one sends that route to a
# hostname that cannot be reached and produces a confusing 500.
export AUTH_SERVICE_URL="http://localhost:8081"
export USER_SERVICE_URL="http://localhost:8082"
export OUTAGE_SERVICE_URL="http://localhost:8083"
export NOTIFICATION_SERVICE_URL="http://localhost:8084"
export COMPLAINT_SERVICE_URL="http://localhost:8085"
export PAYMENT_SERVICE_URL="http://localhost:8086"
export ADMIN_SERVICE_URL="http://localhost:8087"

mkdir -p "$LOGDIR"

# Build first, before killing anything: a failed compile should not leave the
# system half-stopped.
FORCE_BUILD=0
[[ "$1" == "build" ]] && FORCE_BUILD=1

for svc in $SERVICES; do
  name="${svc%%:*}"
  jar="$ROOT/$name/target/$name-1.0.0.jar"
  if [[ $FORCE_BUILD -eq 1 || ! -f "$jar" ]]; then
    echo "building $name ..."
    if ! (cd "$ROOT/$name" && mvn -q clean package -DskipTests); then
      echo "ERROR: build failed for $name — nothing was stopped or started"
      exit 1
    fi
  fi
done

stop_all

for svc in $SERVICES; do
  name="${svc%%:*}"
  port="${svc##*:}"
  nohup java -jar "$ROOT/$name/target/$name-1.0.0.jar" > "$LOGDIR/$name.log" 2>&1 &
  echo "starting $name on :$port   (log: .logs/$name.log)"
done

echo -n "waiting for health checks"
for i in {1..90}; do
  ready=1
  for svc in $SERVICES; do
    [[ "$(health "${svc##*:}")" == "200" ]] || ready=0
  done
  if [[ $ready -eq 1 ]]; then
    echo ""
    echo ""
    echo "ALL UP"
    show_status
    echo ""
    echo "  Gateway   http://localhost:8080   (everything routes through here)"
    echo "  Frontend  cd frontend && npm run dev   -> http://localhost:5173"
    echo ""
    echo "  Swagger:  8081 auth | 8082 user | 8083 outage | 8084 notification"
    echo "            8085 complaint | 8086 payment | 8087 admin"
    echo "            e.g. http://localhost:8087/swagger-ui/index.html"
    exit 0
  fi
  echo -n "."
  sleep 1
done

echo ""
echo "TIMEOUT — not everything came up:"
show_status
echo ""
echo "Check the logs of anything marked ○, e.g.:  tail -40 .logs/<service>.log"
exit 1
