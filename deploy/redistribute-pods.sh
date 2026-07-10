#!/bin/bash
# Pod 重新分配 — master 承载主要服务，两个 slave 各 2 个
set -e

NS=bysj
MASTER=zabbix-monitor
SLAVE1=slave1
SLAVE2=slave2

echo "=== 分配方案 ==="
echo "master: gateway, cart-service, order-service, coupon-service"
echo "slave1: review-service, refund-service"
echo "slave2: user-service, mailadmin"

assign() {
  local svc=$1 node=$2
  echo "Assigning $svc -> $node"
  kubectl patch deployment $svc -n $NS -p "{\"spec\":{\"template\":{\"spec\":{\"nodeName\":\"$node\"}}}}"
  kubectl rollout status deployment/$svc -n $NS --timeout=60s 2>/dev/null || true
}

# master
assign gateway        $MASTER
assign cart-service   $MASTER
assign order-service  $MASTER
assign coupon-service $MASTER

# slave1
assign review-service $SLAVE1
assign refund-service $SLAVE1

# slave2
assign user-service   $SLAVE2
assign mailadmin      $SLAVE2

echo ""
echo "=== 最终分布 ==="
sleep 10
kubectl get pods -n $NS -o wide --sort-by=.spec.nodeName | grep -E 'NAME|Running'
