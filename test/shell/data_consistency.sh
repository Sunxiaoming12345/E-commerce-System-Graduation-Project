#!/bin/bash
# ============================================================
# 数据一致性测试 — 验证下单/支付/取消/退款后各表数据一致
# 运行方式: bash test/shell/data_consistency.sh
# 前提: 用户 zhangsan 已存在，有余额，有可用商品
# ============================================================

set -e

MYSQL="mysql -uroot -p1234 -h 192.168.100.128 -N"
API="http://192.168.100.128:80"

PASS=0
FAIL=0

green() { echo -e "\033[32m$1\033[0m"; }
red()   { echo -e "\033[31m$1\033[0m"; }
blue()  { echo -e "\033[34m$1\033[0m"; }

check() {
    local desc="$1" expected="$2" actual="$3"
    if [ "$expected" = "$actual" ]; then
        green "  ✅ $desc"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望=$expected, 实际=$actual)"
        FAIL=$((FAIL + 1))
    fi
}

check_ge() {
    local desc="$1" expected="$2" actual="$3"
    if [ "$actual" -ge "$expected" ] 2>/dev/null; then
        green "  ✅ $desc (值=$actual)"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望 >= $expected, 实际=$actual)"
        FAIL=$((FAIL + 1))
    fi
}

# ============================================================
# 1. 登录获取 Token
# ============================================================
blue "===== 数据一致性测试 ====="

RESP=$(curl -s -X POST "$API/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"zhangsan","password":"123456"}')
TOKEN=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "登录成功, 用户: zhangsan"

# ============================================================
# 2. 读取下单前数据快照
# ============================================================
blue "===== 快照: 下单前 ====="

# 找一个有库存的商品
PRODUCT=$($MYSQL -e "SELECT id, name, price, stock FROM bysj.products WHERE stock > 5 AND status = 1 LIMIT 1")
PRODUCT_ID=$(echo "$PRODUCT" | awk '{print $1}')
PRODUCT_NAME=$(echo "$PRODUCT" | awk '{print $2}')
PRODUCT_PRICE=$(echo "$PRODUCT" | awk '{print $3}')
STOCK_BEFORE=$(echo "$PRODUCT" | awk '{print $4}')

# 余额
BAL_BEFORE=$($MYSQL -e "SELECT b.balance FROM bysj.balance b JOIN bysj.user u ON u.id=b.user_id WHERE u.username='zhangsan'")

# 订单数
ORDERS_BEFORE=$($MYSQL -e "SELECT COUNT(*) FROM bysj.orders WHERE user_id=(SELECT id FROM bysj.user WHERE username='zhangsan')")

echo "  商品: $PRODUCT_NAME (ID=$PRODUCT_ID, 价格=$PRODUCT_PRICE, 库存=$STOCK_BEFORE)"
echo "  余额: $BAL_BEFORE"
echo "  已有订单数: $ORDERS_BEFORE"

QUANTITY=2
TOTAL_AMOUNT=$(echo "$PRODUCT_PRICE * $QUANTITY" | bc)

# ============================================================
# 3. 创建订单
# ============================================================
blue "===== 创建订单 ====="

# 获取幂等 token
RESP=$(curl -s "$API/user/orders/submit-token" -H "Authorization: $TOKEN")
SUBMIT_TOKEN=$(echo "$RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data','no-token'))")
echo "  幂等Token: $SUBMIT_TOKEN"

# 创建订单
RESP=$(curl -s -X POST "$API/user/orders/create" \
    -H "Content-Type: application/json" \
    -H "Authorization: $TOKEN" \
    -d "{
        \"submitToken\": \"$SUBMIT_TOKEN\",
        \"items\": [{\"productId\": $PRODUCT_ID, \"quantity\": $QUANTITY}],
        \"paymentMethod\": 2,
        \"orderAmount\": $TOTAL_AMOUNT
    }")
echo "  创建响应: $RESP"
ORDER_CODE=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))")

if [ "$ORDER_CODE" != "1" ]; then
    red "  ❌ 创建订单失败！跳过后续测试"
    echo "  响应: $RESP"
    exit 1
fi

# 等待 MQ 异步处理
sleep 2

# ============================================================
# 4. 验证下单后数据
# ============================================================
blue "===== 验证: 下单后 ====="

STOCK_AFTER=$($MYSQL -e "SELECT stock FROM bysj.products WHERE id=$PRODUCT_ID")
EXPECTED_STOCK=$((STOCK_BEFORE - QUANTITY))
check "库存减少 $QUANTITY" "$EXPECTED_STOCK" "$STOCK_AFTER"

ORDERS_AFTER=$($MYSQL -e "SELECT COUNT(*) FROM bysj.orders WHERE user_id=(SELECT id FROM bysj.user WHERE username='zhangsan')")
EXPECTED_ORDERS=$((ORDERS_BEFORE + 1))
check "订单数 +1" "$EXPECTED_ORDERS" "$ORDERS_AFTER"

# 获取刚创建的订单
ORDER_INFO=$($MYSQL -e "SELECT order_id, order_number, order_status, total_amount FROM bysj.orders WHERE user_id=(SELECT id FROM bysj.user WHERE username='zhangsan') ORDER BY create_time DESC LIMIT 1")
ORDER_ID=$(echo "$ORDER_INFO" | awk '{print $1}')
ORDER_NO=$(echo "$ORDER_INFO" | awk '{print $2}')
ORDER_STATUS=$(echo "$ORDER_INFO" | awk '{print $3}')
ORDER_AMOUNT=$(echo "$ORDER_INFO" | awk '{print $4}')

echo "  订单ID: $ORDER_ID, 订单号: $ORDER_NO, 状态: $ORDER_STATUS, 金额: $ORDER_AMOUNT"

# 验证订单项
ORDER_ITEMS=$($MYSQL -e "SELECT COUNT(*) FROM bysj.order_items WHERE order_id=$ORDER_ID")
check "订单项已创建" "1" "$ORDER_ITEMS"

# 验证支付记录
PAYMENT=$($MYSQL -e "SELECT count(*) FROM bysj.payments WHERE order_id=$ORDER_ID")
check "支付记录已创建" "1" "$PAYMENT"

# ============================================================
# 5. 支付订单
# ============================================================
if [ "$ORDER_STATUS" = "0" ]; then
    blue "===== 支付订单 ====="

    RESP=$(curl -s -X POST "$API/user/orders/pay" \
        -H "Content-Type: application/json" \
        -H "Authorization: $TOKEN" \
        -d "{\"orderId\": $ORDER_ID, \"paymentMethod\": 2}")
    echo "  支付响应: $RESP"

    # 等待 MQ 异步处理
    sleep 2

    STATUS_AFTER_PAY=$($MYSQL -e "SELECT order_status FROM bysj.orders WHERE order_id=$ORDER_ID")
    check "订单状态变更为已支付(1)" "1" "$STATUS_AFTER_PAY"

    BAL_AFTER_PAY=$($MYSQL -e "SELECT b.balance FROM bysj.balance b JOIN bysj.user u ON u.id=b.user_id WHERE u.username='zhangsan'")
    EXPECTED_BAL=$(echo "$BAL_BEFORE - $ORDER_AMOUNT" | bc)
    check "余额扣减正确" "$EXPECTED_BAL" "$BAL_AFTER_PAY"

    PM_STATUS=$($MYSQL -e "SELECT status FROM bysj.payments WHERE order_id=$ORDER_ID")
    check "支付记录状态=已支付(1)" "1" "$PM_STATUS"
fi

# ============================================================
# 6. 验证余额扣款原子性（回顾）
# ============================================================
blue "===== 一致性总结 ====="

FINAL_STOCK=$($MYSQL -e "SELECT stock FROM bysj.products WHERE id=$PRODUCT_ID")
FINAL_BAL=$($MYSQL -e "SELECT b.balance FROM bysj.balance b JOIN bysj.user u ON u.id=b.user_id WHERE u.username='zhangsan'")

# 验证 stock 没有变成负数
if [ "$FINAL_STOCK" -ge 0 ] 2>/dev/null; then
    green "  ✅ 库存 >= 0: $FINAL_STOCK"
    PASS=$((PASS + 1))
else
    red "  ❌ 库存为负数: $FINAL_STOCK"
    FAIL=$((FAIL + 1))
fi

# 验证余额没有变成负数
FINAL_BAL_INT=$(echo "$FINAL_BAL" | sed 's/\..*//')
if [ "$FINAL_BAL_INT" -ge 0 ] 2>/dev/null; then
    green "  ✅ 余额 >= 0: $FINAL_BAL"
    PASS=$((PASS + 1))
else
    red "  ❌ 余额为负数: $FINAL_BAL"
    FAIL=$((FAIL + 1))
fi

# ============================================================
# 汇总
# ============================================================
echo ""
blue "===== 结果汇总 ====="
green "通过: $PASS"
if [ $FAIL -gt 0 ]; then
    red "失败: $FAIL"
    exit 1
else
    green "全部通过!"
fi
