#!/bin/bash
# HTTP 并发压测脚本
# 用法: bash bench.sh <token>
API="http://127.0.0.1"
TOKEN="${1:-}"
CONCURRENT=50
TOTAL=500
GREEN='\033[32m'; CYAN='\033[36m'; NC='\033[0m'

bench() {
  local name="$1" method="$2" url="$3" data="$4"
  echo -e "${CYAN}===== $name =====${NC}"
  echo "并发:$CONCURRENT  请求数:$TOTAL"
  rm -f /tmp/bench_*.time
  START=$(date +%s.%N)
  for i in $(seq 1 $TOTAL); do
    (
      T1=$(date +%s%N)
      if [ "$method" = "POST" ]; then
        HTTP=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 -X POST "$API$url" \
          -H 'Content-Type: application/json' -H "Authorization: $TOKEN" -d "$data")
      else
        HTTP=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 "$API$url" \
          -H "Authorization: $TOKEN")
      fi
      T2=$(date +%s%N)
      echo $(( ($T2 - $T1) / 1000000 )) >> /tmp/bench_${HTTP}.time
    ) &
    if [ $((i % CONCURRENT)) -eq 0 ]; then wait; fi
  done; wait
  END=$(date +%s.%N)
  DURATION=$(echo "$END - $START" | bc)
  S200=$(cat /tmp/bench_200.time 2>/dev/null | wc -l)
  S400=$(cat /tmp/bench_400.time 2>/dev/null | wc -l)
  S500=$(cat /tmp/bench_500.time 2>/dev/null | wc -l)
  ALL=$(cat /tmp/bench_*.time 2>/dev/null | sort -n)
  COUNT=$(echo "$ALL" | wc -l)
  AVG=$(echo "$ALL" | awk '{sum+=$1;n++} END{if(n>0) printf "%.0f", sum/n}')
  P99=$(echo "$ALL" | tail -n +$((COUNT - COUNT/100 + 1)) | head -1)
  MAX=$(echo "$ALL" | tail -1)
  QPS=$(echo "scale=0; $COUNT / $DURATION" | bc 2>/dev/null)
  echo "耗时: ${DURATION}s  200: $S200  400: $S400  500: $S500  QPS: ${CYAN}$QPS${NC}"
  echo "响应: avg=${AVG}ms  p99=${P99}ms  max=${MAX}ms"
  echo ""
}

echo "先获取 token..."
if [ -z "$TOKEN" ]; then
  RES=$(curl -s -X POST "$API/user/login" -H 'Content-Type: application/json' -d '{"username":"zhangsan","password":"123456"}')
  TOKEN=$(echo "$RES" | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
  echo "token: ${TOKEN:0:30}..."
fi

bench "GET /products/ 商品列表"  GET  /products/ ""
bench "GET /user/info 用户信息"   GET  /user/info ""
bench "GET /cart/ 购物车"        GET  /cart/ ""

rm -f /tmp/bench_*.time
echo -e "${GREEN}压测完成${NC}"
