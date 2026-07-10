#!/bin/bash
# HTTP 并发压测 — 全接口覆盖
# 用法: bash bench_full.sh
API="http://127.0.0.1"
CONCURRENT=50; TOTAL=500
GREEN='\033[32m'; RED='\033[31m'; CYAN='\033[36m'; NC='\033[0m'

# ========== 获取 token ==========
echo -e "${CYAN}获取测试 token...${NC}"
RES=$(curl -sf -X POST "$API/user/login" -H 'Content-Type: application/json' -d '{"username":"zhangsan","password":"123456"}')
TOKEN=$(echo "$RES" | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
if [ -z "$TOKEN" ]; then echo -e "${RED}登录失败${NC}"; exit 1; fi
echo -e "token: ${TOKEN:0:30}...\n"

# ========== 压测函数 ==========
bench() {
  local name="$1" method="$2" url="$3" data="$4"
  echo -e "${CYAN}===== $name =====${NC}"
  echo "  并发:$CONCURRENT  请求:$TOTAL"
  rm -f /tmp/bench_*.time

  START=$(date +%s.%N)
  for i in $(seq 1 $TOTAL); do
    (
      T1=$(date +%s%N)
      if [ "$method" = "POST" ]; then
        HTTP=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 -X POST "$API$url" \
          -H 'Content-Type: application/json' -H "Authorization: $TOKEN" -d "${data:-{}}")
      elif [ "$method" = "PUT" ]; then
        HTTP=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 -X PUT "$API$url" \
          -H 'Content-Type: application/json' -H "Authorization: $TOKEN" -d "${data:-{}}")
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
  ALL=$(cat /tmp/bench_*.time 2>/dev/null | sort -n)
  COUNT=$(echo "$ALL" | grep -c . 2>/dev/null || echo 1)
  AVG=$(echo "$ALL" | awk '{sum+=$1;n++} END{if(n>0) printf "%.0f", sum/n; else print "N/A"}')
  P99=$(echo "$ALL" | tail -n +$((COUNT - COUNT/100)) 2>/dev/null | head -1)
  MAX=$(echo "$ALL" | tail -1)
  QPS=$(echo "scale=0; $COUNT / $DURATION" | bc 2>/dev/null)
  echo "  QPS: ${CYAN}$QPS${NC}  成功200: $S200  耗时: ${DURATION}s  avg=${AVG}ms  p99=${P99}ms  max=${MAX}ms"
}

# ==================== 免认证接口 ====================
# ==================== 商品浏览（高频读） ====================
echo -e "\n${CYAN}========== 商品浏览（高频读）==========${NC}"

bench "GET /products/ 商品列表"      GET /products/ ""
bench "GET /user/products/recommended" GET /user/products/recommended ""
bench "GET /user/products/categories"  GET /user/products/categories ""
bench "GET /user/products/detail/1"    GET /user/products/detail/1 ""

# ==================== 用户信息（缓存读） ====================
echo -e "\n${CYAN}========== 用户信息 ==========${NC}"

bench "GET /user/info 用户信息"     GET /user/info ""
bench "GET /user/balance/info 余额" GET /user/balance/info ""

# ==================== 购物车 ====================
echo -e "\n${CYAN}========== 购物车 ==========${NC}"

bench "GET /user/shopCart/list 购物车列表" GET /user/shopCart/list ""

# ==================== 订单 ====================
echo -e "\n${CYAN}========== 订单 ==========${NC}"

bench "GET /user/orders/myOrders 订单列表" GET "/user/orders/myOrders?page=1&pageSize=10" ""
bench "GET /user/orders/stats 订单统计"    GET /user/orders/stats ""

# ==================== 优惠券 ====================
echo -e "\n${CYAN}========== 优惠券 ==========${NC}"

bench "GET /user/coupons/my 我的优惠券"       GET /user/coupons/my ""
bench "GET /user/coupons/available 可领优惠券" GET /user/coupons/available ""

# ==================== 评价 ====================
echo -e "\n${CYAN}========== 评价 ==========${NC}"

bench "GET /user/reviews/product/1 商品评价" GET /user/reviews/product/1 ""

# ==================== 退款 ====================
echo -e "\n${CYAN}========== 退款 ==========${NC}"

bench "GET /user/refunds/my 退款列表" GET /user/refunds/my ""

# ========== 清理与汇总 ==========
rm -f /tmp/bench_*.time
echo ""
echo -e "${GREEN}========== 全部压测完成 ==========${NC}"
echo "并发: $CONCURRENT  每接口请求: $TOTAL"
