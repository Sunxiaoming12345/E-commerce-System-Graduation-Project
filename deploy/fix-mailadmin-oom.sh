#!/bin/bash
# 紧急修复 mailadmin OOM 循环：缩容 → 清理 → 移到 master → 恢复
set -e

echo "=== 1. 缩容 mailadmin ==="
kubectl scale deployment mailadmin -n bysj --replicas=0

echo "=== 2. 清理 OOM pod ==="
kubectl delete pods -n bysj -l app=mailadmin --force --grace-period=0 2>/dev/null || true

echo "=== 3. 绑定到 master ==="
kubectl patch deployment mailadmin -n bysj -p '{"spec":{"template":{"spec":{"nodeName":"zabbix-monitor"}}}}'

echo "=== 4. 恢复副本 ==="
kubectl scale deployment mailadmin -n bysj --replicas=1

echo "=== 5. 等待就绪 ==="
kubectl rollout status deployment/mailadmin -n bysj --timeout=60s

echo "=== 6. 最终 Pod 分布 ==="
kubectl get pods -n bysj -o wide --sort-by=.spec.nodeName 2>/dev/null | grep -E 'NAME|Running'

echo "=== 完成 ==="
