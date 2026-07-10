# 故障排查记录

---

## 抽奖模块问题集（2026-07-07）

### 1. 用户抽奖报"服务异常，请稍后重试"

**现象**：前端 `POST /user/lottery/spin` HTTP 200，body `{"code":0,"msg":"服务异常，请稍后重试"}`

**排查**：coupon-service 日志 `调用 user-service 扣减余额失败: 404 /user/balance/internal/decrease`

**根因**：`/internal/decrease` 接口在代码中存在，但 user-service pod 运行的是旧镜像（未重新部署）。

**修复**：重建 user-service JAR → docker commit → 导入 k3s containerd → rollout restart

---

### 2. 奖品名称全是乱码

**现象**：管理端奖池列表 `prize_name` 显示为 `????1`、`æ»¡100å‡ä¼˜æƒ åˆ¸` 等乱码。

**排查**：`docker exec mysql mysql -uroot -p1234 -e "SELECT prize_name FROM lottery_pool"` 出乱码，加 `--default-character-set=utf8mb4` 后正常。

**根因**：之前执行 `seed-lottery.sql` 时 MySQL 客户端未指定字符集，中文被以错误编码写入数据库。

**修复**：
```sql
-- 用 utf8mb4 重新写入所有奖品名称
UPDATE bysj.lottery_pool SET prize_name='谢谢参与' WHERE id=1;
UPDATE bysj.lottery_pool SET prize_name='满50减10优惠券' WHERE id=2;
-- ... 共 8 条
```

---

### 3. 从库复制延迟（主库 UPDATE 不到从库）

**现象**：管理端修改抽奖配置后，用户端看到的是旧值。

**排查**：`SHOW SLAVE STATUS` → `Slave_SQL_Running: No`，`Last_Errno: 1062`，`Duplicate entry '1' for key 'lottery_config.PRIMARY'`

**根因**：种子数据的 INSERT 在从库重复执行，SQL 线程卡住，后续所有 binlog（包括 UPDATE）积压。

**修复**：
```sql
STOP SLAVE;
SET GLOBAL SQL_SLAVE_SKIP_COUNTER = 1;
START SLAVE;
-- 重复直到 Seconds_Behind_Master = 0
```

---

### 4. Docker system prune -af 导致大量 pod ImagePullBackOff

**现象**：清理磁盘后，gateway、cart-service、order-service、mailadmin 等变成 `ImagePullBackOff`

**根因**：
1. `docker system prune -af` 删除了 master 节点所有 Docker 镜像（包括基础镜像）
2. k3s 的 containerd `k8s.io` 命名空间中原本没有 bysj 镜像
3. Pod 被调度到 master → containerd 无镜像 → 尝试从 Docker Hub 拉 → 无外网 → ImagePullBackOff

**关键知识点**：
- `k3s ctr images import` 默认导入到 **`default`** 命名空间，kubelet 用的是 **`k8s.io`** 命名空间
- 正确导入命令：`ctr -a /run/k3s/containerd/containerd.sock -n k8s.io images import xx.tar`

**修复**：从 worker 节点 containerd 导出镜像 → 传到 master → 导入 k8s.io 命名空间 → 删除卡住的 pod

---

### 5. Gateway 返回 500（lottery 路由未匹配）

**现象**：`GET /user/lottery/prizes` 返回 500，body `{"code":0,"msg":"系统异常，请稍后重试"}`

**排查**：
1. 直接访问 coupon-service pod 内部 → 返回正常 ✅
2. Gateway 日志 → `ResourceWebHandler ... 404 NOT_FOUND`（说明路由表中无 lottery 路由）

**根因**：gateway 运行的是旧镜像，`application.yml` 中的 lottery 路由是后续提交才添加的。

**修复**：用最新 gateway JAR 重建镜像 → 导入 containerd → rollout restart

---

### 6. 抽奖次数限制改为可配置

**需求**：取消硬编码的"每天一次"限制，改为管理端可配置，0 = 不限制。

**改动**：
- DB：`lottery_config` 表加 `daily_limit INT DEFAULT 1`
- 后端：`canSpinToday()` 改为读取配置的 `dailyLimit`
- 前端：按钮显示"剩余 X 次"或"不限制次数"

---

## 标准部署流程（Dockerfile + JAR → k3s）

本地 Windows（有网）：
```bash
mvn clean package -pl xxx -am -DskipTests
docker build -t bysj-xxx:latest .
docker save bysj-xxx:latest -o xxx.tar
scp xxx.tar root@192.168.100.128:/tmp/
```

VM master（无网）：
```bash
docker load -i /tmp/xxx.tar                           # 导入到 Docker
docker create --name tmp bysj-xxx:latest              # 创建临时容器
docker cp app.jar tmp:/app/app.jar                    # 替换 JAR
docker commit tmp bysj-xxx:latest                     # 提交为新镜像
docker rm tmp
docker save bysj-xxx:latest -o /tmp/xxx-new.tar       # 导出

# 导入到 k3s containerd（关键：-n k8s.io）
ctr -a /run/k3s/containerd/containerd.sock -n k8s.io images import /tmp/xxx-new.tar

# 分发到其他节点
scp /tmp/xxx-new.tar 192.168.100.129:/tmp/
ssh 192.168.100.129 "ctr -a /run/k3s/containerd/containerd.sock -n k8s.io images import /tmp/xxx-new.tar"
# slave2 同理

# 滚动重启
kubectl rollout restart deploy/xxx -n bysj
kubectl get pods -n bysj -l app=xxx -o wide
```

前端：
```bash
npm run build
scp -r dist/* root@192.168.100.128:/root/nginx/html/user/   # 或 admin/
```

---

## 历史问题（2026-06）

### frp 返回 404

Docker 网桥 `microservice-net` 状态 DOWN 或 IP 丢失，frpc 无法转发请求到 nginx。详见 git 历史 `TROUBLESHOOTING.md`。

### Gateway 登录 400

`Path=/user/**` 通配路由抢在本地 LoginController 之前。改为 `Path=/user/info, /user/balance/**` 精确匹配。

### MinIO 图片 403

桶访问策略为私有，`mc anonymous set download local/images` 设为公开读。

### K8s Nacos 注册 IP 错误

pod 用 `status.hostIP` 注册 → 跨节点 Connection refused。删除 `SPRING_CLOUD_NACOS_DISCOVERY_IP` 让 pod 自动用 pod IP。
