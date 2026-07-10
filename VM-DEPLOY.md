# 代码变更 → VM 部署操作指南

## 环境概览

| 组件 | 方式 | 说明 |
|------|------|------|
| **k3s 集群** | 3 节点 | master: `zabbix-monitor` (192.168.100.128), slave1: 192.168.100.129, slave2: 192.168.100.130 |
| **微服务** | k3s Deployment | namespace: `bysj`, 镜像策略: `IfNotPresent` |
| **Nginx 前端** | Docker 容器 | `frontend-nginx`, 静态文件 bind mount 自 `/root/nginx/html/` |
| **基础设施** | Docker 容器 | MySQL, Redis, Nacos, RabbitMQ, MinIO（非 k3s 管理） |

---

## 一、后端微服务更新

### 1.1 本地 Maven 构建

```bash
# 在 Windows 项目根目录执行
cd D:\workspace\dkd\bysj

# 构建指定模块（-am 自动构建依赖模块）
mvn clean package -pl coupon-service -am -DskipTests -q

# JAR 输出位置
# coupon-service/target/coupon-service-1.0-SNAPSHOT.jar
```

### 1.2 传输 JAR 到 VM master 节点

```bash
scp coupon-service/target/coupon-service-1.0-SNAPSHOT.jar \
    root@192.168.100.128:/root/bysj/deploy/coupon-service/app.jar
```

### 1.3 在 VM master 上更新 Docker 镜像

> **为什么这样操作**：VM 无法访问外网拉取基础镜像，所以不能 `docker build`，只能基于已有镜像替换 JAR 层。
> **镜像可能在 containerd 不在 Docker**：`docker system prune` 或重启后 Docker 镜像会丢失，但 k3s containerd 中仍有镜像。先导出再操作更可靠。

```bash
ssh root@192.168.100.128

# 0. 如果 Docker 中没有该镜像，先从 containerd 导出（查看镜像名用 ctr images ls）
ctr -a /run/k3s/containerd/containerd.sock -n k8s.io images export /tmp/coupon-base.tar \
    docker.io/library/bysj-coupon-service:latest
docker load -i /tmp/coupon-base.tar

# 1. 基于旧镜像创建容器 → 替换 JAR → 提交为新镜像
docker create --name coupon-tmp bysj-coupon-service:latest
docker cp /root/bysj/deploy/coupon-service/app.jar coupon-tmp:/app/app.jar
docker commit coupon-tmp bysj-coupon-service:latest
docker rm coupon-tmp

# 2. 导出镜像为 tar 并导入 k3s containerd
docker save bysj-coupon-service:latest -o /tmp/coupon.tar
k3s ctr images import /tmp/coupon.tar

# 3. 清理临时文件
rm -f /tmp/coupon-base.tar /tmp/coupon.tar
```

### 1.4 重启 k3s Deployment

```bash
kubectl rollout restart deployment/coupon-service -n bysj

# 验证
kubectl get pods -n bysj | grep coupon-service
kubectl logs -n bysj -l app=coupon-service --tail=20
```

---

## 二、前端更新

### 2.1 用户端 (user-web)

```bash
cd D:\workspace\dkd\bysj\user-web
npm run build
scp -r dist/* root@192.168.100.128:/root/nginx/html/user/
```

### 2.2 管理端 (admin-web)

```bash
cd D:\workspace\dkd\bysj\admin-web
npm run build
scp -r dist/* root@192.168.100.128:/root/nginx/html/admin/
```

> Nginx 静态文件目录 bind mount 自 VM 宿主机：
> - 用户端：`/root/nginx/html/user/` → 容器内 `/usr/share/nginx/html/user/`（端口 80）
> - 管理端：`/root/nginx/html/admin/` → 容器内 `/usr/share/nginx/html/admin/`（端口 81）
>
> 文件复制后直接生效，无需重启 nginx 容器。

---

## 三、常用 k3s 命令速查

```bash
# 查看所有 pod 状态
kubectl get pods -n bysj -o wide

# 查看某个 Deployment 的镜像
kubectl get deploy -n bysj coupon-service -o jsonpath='{.spec.template.spec.containers[0].image}'

# 查看 pod 日志
kubectl logs -n bysj -l app=coupon-service --tail=50 -f

# 查看 Deployment 详情
kubectl describe deploy -n bysj coupon-service

# 手动重启某个服务
kubectl rollout restart deployment/<服务名> -n bysj

# 查看节点列表
kubectl get nodes -o wide

# 查看服务暴露端口
kubectl get svc -n bysj
```

---

## 四、各微服务对应关系

| 服务 | k3s Deployment | 端口 | 本地源码模块 |
|------|---------------|------|-------------|
| Gateway | `gateway` | 8080 (NodePort:30080) | `gateway/` |
| MailAdmin | `mailadmin` | 8081 | `mailadmin/` |
| User Service | `user-service` | 8083 | `user-service/` |
| Cart Service | `cart-service` | 8084 | `cart-service/` |
| Order Service | `order-service` | 8085→8082 | `order-service/` |
| Coupon Service | `coupon-service` | 8086 | `coupon-service/` |
| Refund Service | `refund-service` | 8087 | `refund-service/` |
| Review Service | `review-service` | 8088 | `review-service/` |

---

## 五、注意事项

1. **VM 无外网**：不能 `docker build`（无法 pull 基础镜像），只能 `docker commit` 更新已有镜像
2. **k3s 使用 containerd**：`docker images` 中的镜像 ≠ k3s 可用的镜像，必须通过 `k3s ctr images import` 导入
3. **`imagePullPolicy: IfNotPresent`**：每个可能调度到的节点都需要导入镜像
4. **基础设施容器不受 k3s 管理**：`bysj-mysql`、`redis-master`、`bysj-nacos` 等是独立 Docker 容器，不要用 kubectl 操作它们
5. **前端访问入口**：Nginx 容器监听 80/81 端口，API 请求经 Nginx → Gateway (NodePort 30080) → 各微服务
6. **⚠️ 不要在 VM 上 docker run 微服务 JAR**：`docker run` 启动的临时容器会自动注册到 Nacos，和 k3s Pod 形成双实例，网关轮询会导致 50% 请求 500。调试完立即 `docker rm -f`，或加 `--rm`

## 六、磁盘清理

每次编译部署会产生大量中间文件，定期清理避免磁盘占满（磁盘不足会导致 k3s 节点被驱逐）。

### 本地（Windows）

```bash
# 清理所有 Maven target 目录
for dir in */target; do rm -rf "$dir"; done
```

### VM 所有节点

```bash
# master
ssh root@192.168.100.128 "docker system prune -a -f && kubectl delete pods -n bysj --field-selector=status.phase=Failed --force"

# slave1
ssh root@192.168.100.129 "docker system prune -a -f"

# slave2
ssh root@192.168.100.130 "docker system prune -a -f"
```

## 七、常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| API 请求交替 200/500 | Nacos 有残留实例（docker run 的临时容器） | 查 `nacos/v1/ns/instance/list`，删残留容器 |
| Pod 全被 Evicted | 节点磁盘满触发 DiskPressure | 清理磁盘（第六节） |
| ImagePullBackOff | 镜像未导入该节点 | 从有镜像的节点 `k3s ctr image export` → `import` |
| kubectl 命令在 slave 报 connection refused | kubectl 只能连 master | 在 master 上执行 kubectl |
