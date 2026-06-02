# 部署信息

## 虚拟机

| 项目 | 值 |
|------|-----|
| IP | 192.168.100.128 |
| 系统 | CentOS Linux 7 (Core) |
| 账号 | root |
| 密码 | 1234 |
| SSH | 已配置密钥，免密登录 `ssh root@192.168.100.128` |

## 架构概览

```
                    ┌──────────────────────────────────┐
  :80 (用户端)  ──▶ │  frontend-nginx (nginx:1.20.2)   │
  :81 (管理端)  ──▶ │  bind mount: /opt/frontend/      │
                    └──────────┬───────────────────────┘
                               │ proxy_pass
                               ▼
                    ┌──────────────────────────────────┐
                    │  bysj-gateway (:8080)             │  ← Spring Cloud Gateway
                    └──────┬───────────────────────────┘
           ┌───────────────┼───────────────┬───────────────┬───────────────┐
           ▼               ▼               ▼               ▼               ▼
    mailadmin         mailuser       user-service    cart-service   order-service
     :8081             :8082           :8083           :8084           :8085
           │               │               │               │               │
           └───────────────┴───────┬───────┴───────────────┴───────────────┘
                                   │
    ┌──────────────────────────────┼──────────────────────────────┐
    │              基础设施（独立容器）                              │
    │  MySQL (:3306)  Nacos (:8848)  Redis (:6379)                │
    │  RabbitMQ (:5672)  MinIO (:9000)                            │
    └─────────────────────────────────────────────────────────────┘
```

## 容器清单

### 基础设施

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| bysj-mysql | mysql:8.0 | 3306 | 数据库，root/1234 |
| nacos | nacos/nacos-server:v2.2.3 | 8848, 9848 | 注册&配置中心 |
| redis | redis:7-alpine | 6379 | 缓存 |
| my-rabbitmq | rabbitmq:3.12-management-alpine | 5672, 15672 | 消息队列，管理后台 :15672 |
| minio-2024 | minio/minio | 9000, 9001 | 对象存储，控制台 :9001 |

### 微服务

| 容器名 | 端口 | 说明 |
|--------|------|------|
| bysj-gateway | 8080 | Spring Cloud Gateway 网关 |
| bysj-mailadmin | 8081 | 管理端服务 |
| bysj-mailuser | 8082 | 用户端服务 |
| bysj-user-service | 8083 | 用户服务 |
| bysj-cart-service | 8084 | 购物车服务 |
| bysj-order-service | 8085 | 订单服务 |

### 前端

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| frontend-nginx | nginx:1.20.2 | 80, 81 | 静态文件 + 反向代理 |

**网络**: 所有容器通过 `microservice-net`（bridge）通信。

## 访问地址

| 端 | 地址 | 说明 |
|----|------|------|
| 用户端 | http://192.168.100.128:80 | 商城前端 |
| 管理端 | http://192.168.100.128:81 | 后台管理 |
| Nacos | http://192.168.100.128:8848/nacos | 服务注册中心 |
| RabbitMQ | http://192.168.100.128:15672 | 消息队列管理（guest/guest） |
| MinIO | http://192.168.100.128:9001 | 对象存储控制台 |

## Docker Compose 文件

项目有两个 compose 文件：

### `docker-compose.yml`（全量部署）
包含所有基础设施 + 微服务，在 `bysj-network` 上运行。适合**首次裸机部署**。

```bash
docker compose up -d              # 启动
docker compose down               # 停止
docker compose up -d --build      # 重建
```

### `docker-compose.apps.yml`（仅应用）
仅启动 MySQL + 微服务，复用已有的基础设施容器（Redis/Nacos/RabbitMQ/MinIO）。使用外部网络 `microservice-net`。

> **当前生产环境使用的就是这个。**

```bash
# 启动（在项目根目录执行）
docker compose -f docker-compose.apps.yml up -d --build

# 停止
docker compose -f docker-compose.apps.yml down
```

## 前端部署

虚拟机目录结构：

```
/opt/frontend/
├── nginx/
│   └── nginx.conf          # Nginx 主配置
├── admin/                   # 管理端前端文件（挂载到容器 /usr/share/nginx/html/admin）
│   ├── index.html
│   └── assets/
└── user/                    # 用户端前端文件（挂载到容器 /usr/share/nginx/html/user）
    ├── index.html
    └── assets/
```

目录通过 bind mount 挂载到 `frontend-nginx` 容器，上传即生效，无需重启。

### 部署命令（在本机执行）

```bash
# 1. 本地编译
cd admin-web && npx vite build
cd ../user-web && npx vite build

# 2. 上传到虚拟机（免密 SCP）
scp -r admin-web/dist/* root@192.168.100.128:/opt/frontend/admin/
scp -r user-web/dist/* root@192.168.100.128:/opt/frontend/user/

# 3. 浏览器强制刷新（Ctrl+Shift+R）
```

## 常用运维命令

```bash
# SSH 连接
ssh root@192.168.100.128

# 查看所有容器状态
ssh root@192.168.100.128 "docker ps"

# 查看某个服务日志
ssh root@192.168.100.128 "docker logs -f --tail 100 bysj-gateway"

# 重启某个服务
ssh root@192.168.100.128 "docker restart bysj-gateway"

# 重建所有微服务
ssh root@192.168.100.128 "cd /path/to/project && docker compose -f docker-compose.apps.yml up -d --build"
```
