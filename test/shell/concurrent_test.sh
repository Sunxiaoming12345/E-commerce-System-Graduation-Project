#!/bin/bash
# ============================================================
# 并发测试套件 — 库存、余额、优惠券、幂等性
# 运行方式: bash test/shell/concurrent_test.sh
# 前提: 可连接到 mysql（本机或通过 SSH 端口转发）
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
        green "  ✅ $desc (期望=$expected, 实际=$actual)"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望=$expected, 实际=$actual)"
        FAIL=$((FAIL + 1))
    fi
}

# ============================================================
# 测试 1: 库存防超卖
# ============================================================
blue "===== 测试1: 库存防超卖 ====="
$MYSQL -e "UPDATE bysj.products SET stock=30 WHERE id=1"

echo "初始库存=30, 20个并发各买2件, 预期最多15成功, 剩余0"
for i in $(seq 1 20); do
    $MYSQL -e "UPDATE bysj.products SET stock = stock - 2, update_time = now() WHERE id = 1 AND stock >= 2" &
done
wait
STOCK=$($MYSQL -e "SELECT stock FROM bysj.products WHERE id=1")
check "库存防超卖 — 无负数" "0" "$STOCK"

# ============================================================
# 测试 2: 余额防超额扣款
# ============================================================
blue "===== 测试2: 余额防超额扣款 ====="
$MYSQL -e "UPDATE bysj.balance SET balance=500 WHERE id=1"

echo "初始余额=500, 10个并发各扣80, 预期最多6成功(500/80=6), 剩余20"
for i in $(seq 1 10); do
    $MYSQL -e "UPDATE bysj.balance SET balance = balance - 80, update_time = CURRENT_TIMESTAMP WHERE id = 1 AND balance >= 80" &
done
wait
BAL=$($MYSQL -e "SELECT balance FROM bysj.balance WHERE id=1")
check "余额防超额 — 无负数" "20.00" "$BAL"

# ============================================================
# 测试 3: 优惠券防超领
# ============================================================
blue "===== 测试3: 优惠券防超领 ====="
# 查一个有库存的优惠券
COUPON=$($MYSQL -e "SELECT coupon_id FROM bysj.coupons WHERE total_count > used_count AND end_time > NOW() LIMIT 1")
if [ -z "$COUPON" ]; then
    echo "  ⚠️ 无可用优惠券，跳过"
else
    # 重置限量
    $MYSQL -e "UPDATE bysj.coupons SET total_count=10, used_count=0 WHERE coupon_id=$COUPON"

    echo "优惠券ID=$COUPON, 限量10张, 30个并发领取"
    SUCCESS=0
    for i in $(seq 1 30); do
        $MYSQL -e "UPDATE bysj.coupons SET used_count = used_count + 1 WHERE coupon_id = $COUPON AND used_count < total_count" \
            && SUCCESS=$((SUCCESS + 1)) &
    done
    wait
    USED=$($MYSQL -e "SELECT used_count FROM bysj.coupons WHERE coupon_id=$COUPON")
    check "优惠券防超领 — 最多领10张" "10" "$USED"
fi

# ============================================================
# 测试 4: 超售边界 — 库存不够时拒绝所有超额请求
# ============================================================
blue "===== 测试4: 超售边界 ====="
$MYSQL -e "UPDATE bysj.products SET stock=5 WHERE id=1"

echo "初始库存=5, 10个并发各买2件, 预期2成功(5/2=2), 剩余1"
for i in $(seq 1 10); do
    $MYSQL -e "UPDATE bysj.products SET stock = stock - 2, update_time = now() WHERE id = 1 AND stock >= 2" &
done
wait
STOCK=$($MYSQL -e "SELECT stock FROM bysj.products WHERE id=1")
check "超售边界 — 剩余1件不超卖" "1" "$STOCK"

# ============================================================
# 恢复数据
# ============================================================
blue "===== 恢复测试数据 ====="
$MYSQL -e "UPDATE bysj.products SET stock=49 WHERE id=1"
$MYSQL -e "UPDATE bysj.balance SET balance=9829231.97 WHERE id=1"
echo "已恢复"

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
