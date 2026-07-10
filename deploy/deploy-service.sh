#!/bin/bash
# 通用微服务部署脚本
# 用法: ./deploy-service.sh <service-name> [node-ip]
# 示例: ./deploy-service.sh gateway 192.168.100.128
#       ./deploy-service.sh coupon-service 192.168.100.129
set -e

SVC=${1:?usage: $0 <service-name> [node-ip]}
NODE=${2:-192.168.100.128}
JAR="../../${SVC}/target/${SVC}-1.0-SNAPSHOT.jar"

echo "=== Deploying $SVC to $NODE ==="

# 1. 本地编译
cd "$(dirname "$0")/.."
mvn package -pl $SVC -am -DskipTests -q

# 2. 上传 JAR
scp "$JAR" root@$NODE:/tmp/jars/$SVC.jar

# 3. 构建 + 导入 + 重启
ssh root@$NODE "cd /tmp/jars && \
  echo 'FROM bysj-$SVC:latest' > Dockerfile.$SVC && \
  echo 'COPY $SVC.jar /app/app.jar' >> Dockerfile.$SVC && \
  docker build --pull=false -t bysj-$SVC:latest -f Dockerfile.$SVC . && \
  k3s ctr image rm docker.io/library/bysj-$SVC:latest 2>/dev/null || true && \
  docker save bysj-$SVC:latest | k3s ctr image import - && \
  kubectl delete pod -n bysj -l app=$SVC"

echo "=== Done: $SVC ==="
