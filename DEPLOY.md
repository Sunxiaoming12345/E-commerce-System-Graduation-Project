# 部署信息

## 虚拟机

| 项目 | 值 |
|------|-----|
| IP | 192.168.100.128 |
| 系统 | CentOS Linux 7 (Core) |
| 账号 | root |
| 密码 | 1234 |
| SSH | 已配置密钥，免密登录 `ssh root@192.168.100.128` |

## Docker 版本要求（CentOS 7）

CentOS 7 内核 3.10 → Docker CE 最高支持 **24.0.x**（25+ 不再支持）。

```bash
scp deploy/scripts/install-docker-centos7.sh root@192.168.100.128:/opt/
ssh root@192.168.100.128 "bash /opt/install-docker-centos7.sh"
```

仅需 Docker（用于构建镜像 + 基础设施容器），不需要 docker-compose。

## 架构概览

```
                    ┌──────────────────────────────────────────────┐
  :80 (用户端)  ──▶ │  frontend-nginx (nginx:1.20.2, Docker)       │
  :81 (管理端)  ──▶ │  upstream → 192.168.100.128:30080            │
                    └──────────┬───────────────────────────────────┘
                               │ NodePort
            ┌──────────────────┴──────────────────────────────────┐
            ▼                                                      ▼
   ┌──────────────────────┐  k3s cluster (3 nodes)   ┌──────────────────────┐
   │  gateway-svc         │                           │  gateway pod         │
   │  NodePort :30080     │                           │  (多实例, Nacos lb)  │
   └──────────────────────┘                           └──────────┬───────────┘
                                                                 │
        ┌────────────────────────────────────────────────────────┼───────────────┬───────────────┬───────────────┐
        ▼                ▼               ▼               ▼               ▼               ▼               ▼
  mailadmin       user-service    cart-service    order-service    coupon-service   refund-service   review-service
        │                │               │               │               │               │               │
        └────────────────┴───────────────┴───────────────┴───────────────┴───────────────┴───────────────┘
                                             │
         ┌───────────────────────────────────┼───────────────────────────────┐
         │              基础设施（Docker 容器）                               │
         │  MySQL (:3306)  Nacos (:8848)  Redis (:6379)  RabbitMQ (:5672)   │
         │  云服务器: MinIO (110.40.200.239:9000)                             │
         └───────────────────────────────────────────────────────────────────┘
```

**请求链路**: 浏览器 → frontend-nginx (:80/:81) → k3s NodePort (:30080) → gateway pod → 各微服务

## 基础设施（Docker 容器）

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| bysj-mysql | mysql:8.0 | 3306 | 数据库，root/1234 |
| bysj-nacos | nacos/nacos-server:v2.2.3 | 8848, 9848 | 注册&配置中心 |
| redis-master | redis:7-alpine | 6379 | 缓存 |
| my-rabbitmq | rabbitmq:3.12-management-alpine | 5672, 15672 | 消息队列，管理后台 :15672 |
| minio-2024 | minio/minio | 9000, 9001 | 对象存储（运行在 110.40.200.239 云服务器） |

## 微服务（k3s Pod）

| Service | K8s 资源 | 说明 |
|---------|----------|------|
| gateway-svc | NodePort :30080 | Spring Cloud Gateway 网关入口 |
| mailadmin | Deployment | 管理端服务 |
| user-service | Deployment | 用户服务 |
| cart-service | Deployment | 购物车服务 |
| order-service | Deployment | 订单服务 |
| coupon-service | Deployment | 用户优惠券服务 |
| refund-service | Deployment | 用户退款服务 |
| review-service | Deployment | 用户评价服务 |

**命名空间**: `bysj`
**高可用**: Gateway 和 order-service 通过 k3s Deployment 多副本 + Nacos 服务发现实现负载均衡。

## 前端（Docker 容器）

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| frontend-nginx | nginx:1.20.2 | 80, 81 | 静态文件 + 反向代理到 k3s NodePort |

## 访问地址

| 端 | 地址 | 说明 |
|----|------|------|
| 用户端 | http://192.168.100.128:80 | 商城前端 |
| 管理端 | http://192.168.100.128:81 | 后台管理 |
| Nacos | http://192.168.100.128:8848/nacos | 服务注册中心 |
| RabbitMQ | http://192.168.100.128:15672 | 消息队列管理（guest/guest） |
| MinIO | http://110.40.200.239:9001 | 对象存储控制台（云服务器） |

## 微服务部署（k3s）

使用 `k8s/deploy.sh` 构建镜像并部署到 k3s 集群（master + 2 slave）。

```bash
# 在 VM master 节点上执行
cd /root/bysj
bash k8s/deploy.sh
```

部署流程：
1. 为每个微服务 `docker build` 构建镜像
2. `docker save` + `k3s ctr image import` 导入到各节点
3. `kubectl apply -f k8s/<service>.yaml` 部署
4. 等待 Pod 就绪

**注意**：如有新 JAR，需要先 `mvn clean package -DskipTests` 生成 `deploy/<service>/` 下的 JAR 文件，再执行 `deploy.sh`。

## 前端部署

虚拟机目录结构：

```
/root/nginx/
├── nginx.conf              # Nginx 主配置（挂载到容器 /etc/nginx/nginx.conf:ro）
├── html/
│   ├── admin/              # 管理端前端文件（挂载到容器 /usr/share/nginx/html/admin）
│   │   ├── index.html
│   │   └── assets/
│   └── user/               # 用户端前端文件（挂载到容器 /usr/share/nginx/html/user）
│       ├── index.html
│       └── assets/
```

Nginx 通过 `upstream` 块将 API 请求反向代理到 k3s NodePort（`192.168.100.128:30080`）。

目录通过 bind mount 挂载到 `frontend-nginx` 容器，上传即生效，无需重启。

### 部署命令（在本机执行）

```bash
# 1. 本地编译
cd admin-web && npx vite build
cd ../user-web && npx vite build

# 2. 清理旧文件 + 上传到虚拟机（免密 SCP）
ssh root@192.168.100.128 "rm -rf /root/nginx/html/admin/*"
ssh root@192.168.100.128 "rm -rf /root/nginx/html/user/*"
scp -r admin-web/dist/* root@192.168.100.128:/root/nginx/html/admin/
scp -r user-web/dist/* root@192.168.100.128:/root/nginx/html/user/

# 3. 浏览器强制刷新（Ctrl+Shift+R）
```

## 常用运维命令

```bash
# SSH 连接
ssh root@192.168.100.128

# 查看 k3s 集群状态
kubectl get nodes
kubectl get pods -n bysj -o wide
kubectl get svc -n bysj

# 查看某个服务日志
kubectl logs -f --tail 100 -l app=gateway -n bysj

# 重启某个服务
kubectl rollout restart deployment gateway -n bysj

# 查看基础设施容器
ssh root@192.168.100.128 "docker ps"

# 重新部署所有微服务
ssh root@192.168.100.128 "cd /root/bysj && bash k8s/deploy.sh"
```
