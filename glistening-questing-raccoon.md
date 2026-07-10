# 转盘抽奖功能 — 实现方案

## Context

为用户端添加转盘抽奖功能，管理端可配置奖品和概率。复用现有优惠券系统和余额系统。

## 需求概要

- **用户端**：8 格转盘，消耗余额抽奖，每天 1 次
- **奖品类型**：优惠券、余额、实物大奖（手机/电脑等）、谢谢参与
- **管理端**：配置抽奖消耗金额、管理奖品（类型/概率/库存）、查看抽奖记录、实物发货

---

## 数据库设计

### 新表 `lottery_config`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| spin_cost | DECIMAL(10,2) | 每次抽奖消耗余额 |
| update_time | DATETIME | |

### 新表 `lottery_pool`（奖池）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| prize_type | VARCHAR(20) | COUPON / BALANCE / THANKS / PHYSICAL |
| prize_name | VARCHAR(100) | 奖品名称 |
| prize_image | VARCHAR(500) | 奖品图片 |
| coupon_id | BIGINT | 关联 coupons 表（COUPON 类型） |
| balance_amount | DECIMAL(10,2) | 余额金额（BALANCE 类型） |
| probability | DECIMAL(8,4) | 中奖概率（0~1） |
| total_stock | INT | 总库存（PHYSICAL 类型） |
| remaining_stock | INT | 剩余库存 |
| status | TINYINT | 1=启用 0=停用 |

### 新表 `lottery_record`（抽奖记录）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | INT | |
| spin_cost | DECIMAL(10,2) | 本次消耗 |
| prize_type | VARCHAR(20) | |
| prize_id | BIGINT | 关联 lottery_pool |
| prize_name | VARCHAR(100) | |
| coupon_id | BIGINT | |
| balance_amount | DECIMAL(10,2) | |
| fulfillment_status | TINYINT | 0=待发货 1=已发货 2=已收货（实物） |
| shipping_info | VARCHAR(500) | 物流信息 |
| create_time | DATETIME | |

---

## 后端实现

### 1. user-service — 新增内部余额接口
- **文件**：`BalanceController.java` 新增两个端点
- `POST /user/balance/internal/decrease?userId=&amount=` — 扣减余额
- `POST /user/balance/internal/increase?userId=&amount=` — 发放余额
- 通过 `userId` 参数传值（内部调用不走网关，无 user-info header）

### 2. coupon-service — 核心抽奖逻辑
- **新增文件**：
  - `mapper/LotteryMapper.java` — 查奖池、插记录、减库存、今日次数
  - `service/LotteryService.java` + `impl/LotteryServiceImpl.java`
  - `controller/LotteryController.java` — 用户端 API
  - `vo/LotteryPrizeVO.java`、`LotterySpinResultVO.java`、`LotteryRecordVO.java`
  - `config/RestTemplateConfig.java` — 调用 user-service 余额接口
- **用户端 API**（前缀 `/user/lottery`）：
  | 端点 | 方法 | 说明 |
  |------|------|------|
  | `/prizes` | GET | 获取奖池（前端渲染转盘） |
  | `/cost` | GET | 获取抽奖消耗 |
  | `/status` | GET | 检查是否可抽 |
  | `/spin` | POST | 执行抽奖（扣余额→随机→发奖→记录） |
  | `/records` | GET | 我的抽奖记录 |
- **抽奖算法**：加权随机，按 `probability` 累积分布抽取
- **并发控制**：Redis 分布式锁 `lottery:daily:{userId}:{date}`

### 3. mailadmin — 管理端 CRUD
- **新增文件**：
  - `mapper/LotteryMapper.java` + `LotteryMapper.xml`
  - `service/LotteryService.java` + `impl/LotteryServiceImpl.java`
  - `controller/LotteryController.java`
  - `dto/AddPrizeDTO.java`、`EditPrizeDTO.java`、`LotteryConfigDTO.java`、`LotteryRecordPageQueryDTO.java`、`UpdateFulfillmentDTO.java`
- **管理端 API**（前缀 `/admin/lottery`）：
  | 端点 | 方法 | 说明 |
  |------|------|------|
  | `/config` | GET/PUT | 抽奖消耗配置 |
  | `/prizes` | GET | 奖池列表 |
  | `/prizes/{id}` | GET | 奖品详情 |
  | `/prizes` | POST | 新增奖品 |
  | `/prizes` | PUT | 编辑奖品 |
  | `/prizes/{id}` | DELETE | 删除奖品 |
  | `/records` | GET | 分页查询抽奖记录 |
  | `/records/fulfillment` | PUT | 更新实物发货状态 |

### 4. common — 共享实体
- 新增 `entity/LotteryPool.java`、`entity/LotteryRecord.java`
- 新增 `constant/LotteryConstant.java`（prize type、fulfillment status 常量）

### 5. gateway — 路由配置
- `application.yml` 新增两条路由：
  - `/user/lottery/**` → `lb://coupon-service`
  - `/admin/lottery/**` → `lb://mailadmin`

---

## 前端实现

### 用户端 (user-web)

| 文件 | 操作 | 说明 |
|------|------|------|
| `api/lottery.js` | 新增 | 5 个 API 函数 |
| `views/Lottery.vue` | 新增 | 转盘组件 + 抽奖记录 |
| `router/index.js` | 修改 | 新增 `/lottery` 路由 |
| `views/Layout.vue` | 修改 | 导航栏加"抽奖"入口 |

**Lottery.vue 核心设计**：
- 顶部：当前余额 + 抽奖消耗 + 抽奖按钮
- 中间：Canvas/纯 CSS 8 格转盘，奖品由 `/prizes` 返回动态渲染
- 底部：我的抽奖记录列表
- 动画：CSS `transform: rotate()` + `transition`，后端返回 winning prize，前端计算目标角度

### 管理端 (admin-web)

| 文件 | 操作 | 说明 |
|------|------|------|
| `api/lottery.js` | 新增 | 9 个 API 函数 |
| `views/Lottery.vue` | 新增 | 抽奖管理页面（3 个 Tab） |
| `router/index.js` | 修改 | 新增 `/lottery` 路由 |
| `views/Layout.vue` | 修改 | 侧边栏加"抽奖管理"菜单 |

**Lottery.vue Tab 设计**：
1. **抽奖配置**：设置每次抽奖消耗金额
2. **奖池管理**：奖品 CRUD 表格 + 弹窗表单（类型切换联动不同字段）
3. **抽奖记录**：分页列表 + 实物发货弹窗

---

## 实施顺序

| 步骤 | 模块 | 内容 |
|------|------|------|
| 1 | SQL | 创建 3 张表 + 初始数据 |
| 2 | common | LotteryPool、LotteryRecord、LotteryConstant |
| 3 | user-service | 内部余额增减接口 |
| 4 | coupon-service | LotteryMapper、Service、Controller、VOs |
| 5 | mailadmin | LotteryMapper/XML、Service、Controller、DTOs |
| 6 | gateway | 两条新路由 |
| 7 | user-web | API、Lottery.vue、路由、导航 |
| 8 | admin-web | API、Lottery.vue、路由、侧边栏 |
| 9 | 部署验证 | 编译、构建镜像、部署到 K8s、端到端测试 |

---

## 关键设计决策

- **复用 coupon-service**：不新建微服务，减少 Docker/K8s/Nacos 配置
- **RestTemplate 调用 user-service**：项目没有 Feign，用 `@LoadBalanced RestTemplate`
- **内部接口用 userId 参数**：绕过 `BaseContext`（内部调用不走网关，无 user-info header），与用户接口隔离
- **后端决定中奖结果**：前端转盘动画只做展示，防止客户端作弊
- **Redis 每日锁**：防并发重复抽奖
