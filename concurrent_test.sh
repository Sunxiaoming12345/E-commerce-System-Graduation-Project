#!/bin/bash

echo "===== 并发库存原子性测试 ====="
echo ""

# 重置
mysql -uroot -p1234 -N -e "UPDATE bysj.products SET stock=30 WHERE id=1" 2>/dev/null

echo "初始库存: 30 件"
echo "发起 20 个并发请求，每个买 2 件"
echo "理论: 最多成功 15 个 (30 / 2 = 15)，剩余 0"
echo ""

# 并发
for i in $(seq 1 20); do
    mysql -uroot -p1234 -N -e "UPDATE bysj.products SET stock = stock - 2, update_time = now() WHERE id = 1 AND stock >= 2" 2>/dev/null &
done
wait

FINAL=$(mysql -uroot -p1234 -N -e "SELECT stock FROM bysj.products WHERE id=1" 2>/dev/null)
echo "最终库存: $FINAL"
if [ "$FINAL" = "0" ]; then
    echo "结果: ✅ 通过 — 没有超卖 (30 - 15×2 = 0)"
else
    echo "结果: ❌ 失败 — 期望 0，实际 $FINAL"
fi

echo ""
echo "===== 并发余额原子性测试 ====="
echo ""

mysql -uroot -p1234 -N -e "UPDATE bysj.balance SET balance=500 WHERE user_id=1" 2>/dev/null

echo "初始余额: 500"
echo "发起 10 个并发请求，每个扣 80"
echo "理论: 最多成功 6 个 (500 / 80 = 6)，剩余 20"
echo ""

for i in $(seq 1 10); do
    mysql -uroot -p1234 -N -e "UPDATE bysj.balance SET balance = balance - 80, update_time = CURRENT_TIMESTAMP WHERE user_id = 1 AND balance >= 80" 2>/dev/null &
done
wait

FINAL2=$(mysql -uroot -p1234 -N -e "SELECT balance FROM bysj.balance WHERE user_id=1" 2>/dev/null)
echo "最终余额: $FINAL2"
if [ "$FINAL2" = "20.00" ]; then
    echo "结果: ✅ 通过 — 没有超扣 (500 - 6×80 = 20)"
else
    echo "结果: ❌ 失败 — 期望 20.00，实际 $FINAL2"
fi

echo ""
echo "===== 并发超售测试（超过可售量）====="
echo ""

mysql -uroot -p1234 -N -e "UPDATE bysj.products SET stock=5 WHERE id=1" 2>/dev/null

echo "初始库存: 5 件"
echo "发起 10 个并发请求，每个买 2 件"
echo "理论: 最多成功 2 个 (5 / 2 = 2)，剩余 1 件不会被超卖到负数"
echo ""

for i in $(seq 1 10); do
    mysql -uroot -p1234 -N -e "UPDATE bysj.products SET stock = stock - 2, update_time = now() WHERE id = 1 AND stock >= 2" 2>/dev/null &
done
wait

FINAL3=$(mysql -uroot -p1234 -N -e "SELECT stock FROM bysj.products WHERE id=1" 2>/dev/null)
echo "最终库存: $FINAL3"
if [ "$FINAL3" = "1" ]; then
    echo "结果: ✅ 通过 — 没有超卖到负数 (5 - 2×2 = 1，其余 8 个被 WHERE 拒绝)"
else
    echo "结果: ❌ 失败 — 期望 1，实际 $FINAL3"
fi

# 恢复
mysql -uroot -p1234 -N -e "UPDATE bysj.products SET stock=49 WHERE id=1" 2>/dev/null
mysql -uroot -p1234 -N -e "UPDATE bysj.balance SET balance=9829231.97 WHERE user_id=1" 2>/dev/null
echo ""
echo "数据已恢复"
