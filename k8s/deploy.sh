#!/bin/bash
# K8s 部署脚本 — 构建镜像、分发、部署
set -e

SERVICES="gateway order-service cart-service user-service coupon-service refund-service review-service mailadmin chat-service"
MASTER="192.168.100.128"
SLAVES="192.168.100.129 192.168.100.130"

echo "=== 1. 构建 Docker 镜像 (master) ==="
cd /root/bysj
for svc in $SERVICES; do
  echo "Building $svc..."
  docker build -t bysj-$svc:latest -f $svc/Dockerfile . 2>&1 | tail -3
done

echo "=== 2. 导出镜像并分发到所有节点 ==="
for svc in $SERVICES; do
  echo "Exporting bysj-$svc..."
  docker save bysj-$svc:latest -o /tmp/bysj-$svc.tar

  # 导入到 master 的 containerd
  k3s ctr image import /tmp/bysj-$svc.tar 2>/dev/null

  # 分发到 slave
  for slave in $SLAVES; do
    scp /tmp/bysj-$svc.tar root@$slave:/tmp/
    ssh root@$slave "k3s ctr image import /tmp/bysj-$svc.tar && rm /tmp/bysj-$svc.tar"
  done
  rm /tmp/bysj-$svc.tar
done

echo "=== 3. 部署到 K8s ==="
kubectl apply -f /root/bysj/k8s/namespace.yaml
kubectl apply -f /root/bysj/k8s/configmap.yaml
for svc in $SERVICES; do
  kubectl apply -f /root/bysj/k8s/$svc.yaml
done

echo "=== 4. 等待 Pod 就绪 ==="
kubectl wait --for=condition=ready pod -l app=gateway -n bysj --timeout=120s 2>/dev/null || true
kubectl get pods -n bysj -o wide

echo "=== 部署完成 ==="
echo "Gateway NodePort: 30080"
echo "验证: curl http://192.168.100.129:30080/user/login"
