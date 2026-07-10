# 会话记录 — 2026-07-06 (Part 2: 转盘抽奖 + 运维)

## 一、转盘抽奖功能（完整实现）

### 架构
- **用户端**：`/user/lottery/**` → gateway → **coupon-service** (slave1)
- **管理端**：`/admin/lottery/**` → gateway → **mailadmin** (master)

### 数据库（3 张新表）
| 表 | 用途 |
|------|------|
| `lottery_config` | 抽奖配置（spin_cost） |
| `lottery_pool` | 奖池（奖品+概率+库存） |
| `lottery_record` | 抽奖记录 |

种子数据：8 条奖品（谢谢参与 30%、优惠券 2 种、余额 3 种、实物 2 种）

### 新增/修改的文件

**common 模块**
- `entity/LotteryPool.java` — 奖池实体
- `entity/LotteryRecord.java` — 抽奖记录实体
- `constant/LotteryConstant.java` — 常量

**user-service**（内部余额接口）
- `controller/BalanceController.java` — 新增 `POST /internal/decrease`、`POST /internal/increase`

**coupon-service**（抽奖核心，port 8086）
- `mapper/LotteryMapper.java` — 查奖池、插记录、扣库存、今日次数
- `service/LotteryService.java` + `impl/LotteryServiceImpl.java` — 加权随机 + 余额扣除 + 发放奖品
- `controller/LotteryController.java` — `/user/lottery/{prizes,cost,status,spin,records}`
- `vo/LotteryPrizeVO.java`、`LotterySpinResultVO.java`、`LotteryRecordVO.java`
- `config/RestTemplateConfig.java` — `@LoadBalanced RestTemplate`
- `pom.xml` — 添加 `commons-lang3` 依赖

**mailadmin**（管理端 CRUD，port 8081）
- `mapper/LotteryMapper.java` + `LotteryMapper.xml`
- `service/LotteryService.java` + `impl/LotteryServiceImpl.java`
- `controller/LotteryController.java` — `/admin/lottery/{config,prizes,records,fulfillment}`
- `dto/AddPrizeDTO.java`、`EditPrizeDTO.java`、`LotteryConfigDTO.java`、`LotteryRecordPageQueryDTO.java`、`UpdateFulfillmentDTO.java`

**gateway**（路由）
- `application.yml` — 新增 `/user/lottery/**` → coupon-service、`/admin/lottery/**` → mailadmin

**user-web**（用户前端）
- `api/lottery.js` — 5 个 API 函数
- `views/Lottery.vue` — 8 格 CSS 转盘 + 抽奖记录
- `router/index.js` — 新增 `/lottery` 路由
- `views/Layout.vue` — 导航栏加「抽奖」入口（TrophyBase 图标）

**admin-web**（管理前端）
- `api/lottery.js` — 9 个 API 函数
- `views/Lottery.vue` — 3 Tab：配置、奖池管理、抽奖记录+发货
- `router/index.js` — 新增 `/lottery` 路由
- `views/Layout.vue` — 侧边栏加「抽奖管理」（Present 图标）

---

## 二、运维操作

### Pod 分布（最终状态）
| 节点 | Pod | 数量 |
|------|-----|------|
| master (6.5G) | gateway, cart-service, order-service, mailadmin | 4 |
| slave1 (1.8G) | coupon-service, review-service | 2 |
| slave2 (2.2G) | user-service, refund-service | 2 |

### 关键修复
1. **mailadmin OOM 循环**：slave2 内存不足导致 mailadmin 反复创建→OOM→重建，生成数百个僵尸 Pod。通过 scale→0、清理→绑定 master→scale→1 解决
2. **kubectl 僵尸进程**：清理 OOM Pod 的 kubectl 命令残留，占用 ~1GB 内存，kill 后释放
3. **commons-lang3 缺失**：coupon-service 缺少 `commons-lang3` 依赖导致 `NoClassDefFoundError`，在 pom.xml 显式添加解决
4. **gateway Dockerfile 路径错误**：`COPY gateway.jar /app/` 应为 `COPY gateway.jar /app/app.jar`，导致 gateway 一直用旧 JAR
5. **gateway JAR 未更新**：多次 `k3s ctr image import` 后 K3s 仍缓存旧镜像，需先 `rm` 旧镜像再 `import` + 删 Pod 强制重建

---

## 三、MySQL 主从复制

- Master: `bysj-mysql` (3306) → Slave: `bysj-mysql-2` (3307)
- 使用 MySQL 8.0 `CHANGE REPLICATION SOURCE TO` 语法
- `FLUSH LOGS` 切 binlog 解决位点边界问题
- 最终状态：`Replica_IO_Running: Yes, Replica_SQL_Running: Yes, Seconds_Behind_Source: 0`

---

## 四、部署脚本

| 文件 | 用途 |
|------|------|
| `deploy/seed-lottery.sql` | 种子数据（8 条奖品） |
| `deploy/nginx-lottery.conf` | nginx 配置（含 /user/、/admin/ 代理） |
| `deploy/fix-mailadmin-oom.sh` | 紧急修复 mailadmin OOM |
| `deploy/redistribute-pods.sh` | Pod 节点分配脚本 |
| `deploy/fix-nginx.sh` | nginx 容器重建脚本 |

---

## 五、当前状态

- 8/8 微服务全部 Running
- 抽奖后端接口正常（需 JWT Token）
- 管理端 `/admin/lottery/config` 可用
- 前端已编译并部署到 nginx（`:80` 用户端 + `:81` 管理端）
- 3 节点内存充足
