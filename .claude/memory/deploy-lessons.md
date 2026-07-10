---
name: deploy-lessons
description: 部署避坑指南：CentOS 7 + Docker + k3s 常见问题和预防措施
metadata:
  type: project
---

## 架构现状

- **基础设施**：Docker 容器（MySQL, Nacos, Redis, RabbitMQ, MinIO）
- **微服务**：k3s Pod（gateway, mailadmin, user-service, cart-service, order-service, coupon-service, refund-service, review-service）
- **前端**：`frontend-nginx`（Docker 容器，bind mount `/root/nginx/html/`，反向代理到 k3s NodePort 30080）
- **部署脚本**：`k8s/deploy.sh`

## 今天遇到的问题和预防

### 1. 代码变更后必须重新部署
- **预防**：每次改完 Java 代码，本地 `mvn package -DskipTests`，然后执行 `k8s/deploy.sh`

### 2. 前端部署注意路径和清理
- **预防**：
  - 前端挂载路径是 `/root/nginx/html/user/` 和 `/root/nginx/html/admin/`
  - 每次部署前先 `rm -rf` 清空，避免 Vite 哈希文件累积

### 3. 新增依赖要声明
- **预防**：引入新注解/库前，先确认 pom.xml 有对应 dependency

### 4. k3s coredns ImagePullBackOff
- **预防**：k3s 节点无法拉取镜像时，手动 `docker save` + `k3s ctr image import`

## 标准部署流程

```bash
# 1. 本地编译所有模块
mvn package -DskipTests -q

# 2. 上传 JAR 到 VM master
# 3. 在 VM master 上执行部署
ssh root@192.168.100.128 "cd /root/bysj && bash k8s/deploy.sh"

# 4. 验证
kubectl get pods -n bysj -o wide
```
