#!/bin/bash
# 更新 cart-service 到 K8s 集群
set -e

cd /tmp

# 1. 创建最小 Dockerfile
cat > Dockerfile.cart << 'DOCKERFILE'
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY cart-service.jar /app/app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
DOCKERFILE

# 2. 构建镜像
echo "=== Building Docker image ==="
docker build -t bysj-cart-service:latest -f Dockerfile.cart .

# 3. 导入到 k3s containerd
echo "=== Importing to k3s ==="
docker save bysj-cart-service:latest | k3s ctr image import -

# 4. 重启 deployment
echo "=== Restarting deployment ==="
kubectl rollout restart deployment/cart-service -n bysj
kubectl rollout status deployment/cart-service -n bysj --timeout=60s

# 5. 验证
echo "=== Pod status ==="
kubectl get pods -n bysj -l app=cart-service -o wide

echo "=== Done ==="
