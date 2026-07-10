# Redis 缓存设计文档

## 架构

```
所有微服务 → Redis 7（单实例，172.21.0.2:6379）
```

- 无 Spring Cache 注解（`@Cacheable` 等）
- 全部通过 `StringRedisTemplate` / `RedisTemplate` 手动操作
- 公共工具类在 `common` 模块：`CacheUtil`、`IdempotentUtil`、`RedisLockUtil`、`RateLimitInterceptor`
- 所有缓存统一使用 **Cache-Aside** 模式：读 miss 回填，写后 DELETE 失效

---

## 一、业务数据缓存

### 1.1 商品相关（已有）

| 缓存数据 | Key | 数据结构 | TTL | 写入方 | 读取方 |
|---------|-----|---------|-----|--------|--------|
| 推荐商品列表 | `products:recommended` | String (JSON数组) | 1h | cart-service, order-service | cart-service, order-service |
| 全部分类列表 | `categories:all` | String (JSON数组) | 1h | cart-service, order-service | cart-service, order-service |
| 分类商品列表 | `products:category:{categoryId}` | String (JSON数组) | 1h | cart-service, order-service | cart-service, order-service |
| 商品详情（静态字段） | `product:detail:{productId}` | Hash | 30min | cart-service | cart-service |
| 商品库存 | `product:stock:{productId}` | String (int) | 30min | mailadmin, order-service (CacheUtil) | cart-service |

失效触发：商品新增/编辑/上下架/删除 → `products:recommended`、`product:detail:{id}`、`products:category:{id}` / 分类变更 → `categories:all` / 库存变动 → 直接更新值

### 1.2 用户相关（新增）

| 缓存数据 | Key | 数据结构 | TTL | 写入方 | 读取方 | 失效触发 |
|---------|-----|---------|-----|--------|--------|---------|
| 用户信息 | `user:info:{userId}` | String (JSON) | 30min | user-service | user-service | 更新用户信息时 DELETE |
| 订单统计 | `user:orderStats:{userId}` | String (JSON) | 10min | order-service | order-service | 下单/支付/取消/退款时 DELETE（覆盖 Controller + MQ Consumer 共 6 处） |

### 1.3 优惠券相关（新增）

| 缓存数据 | Key | 数据结构 | TTL | 写入方 | 读取方 | 失效触发 |
|---------|-----|---------|-----|--------|--------|---------|
| 可领优惠券 | `coupons:available` | String (JSON数组) | 5min | coupon-service | coupon-service | 用户领券 / 后台增删改优惠券时 DELETE |
| 用户优惠券列表 | `user:coupons:{userId}:{status}` | String (JSON数组) | 5min | coupon-service | coupon-service | 领券/用券/释放券时 DELETE 该用户全部状态缓存 |

### 1.4 评价相关（新增）

| 缓存数据 | Key | 数据结构 | TTL | 写入方 | 读取方 | 失效触发 |
|---------|-----|---------|-----|--------|--------|---------|
| 商品评价列表 | `product:reviews:{productId}` | String (JSON数组) | 10min | review-service | review-service | 用户提交评价 / 后台审核/删除评价时 DELETE |

---

## 二、功能型 Redis

### 2.1 接口限流

| 接口 | Key 模板 | 限制 | 窗口 | Key 类型 |
|------|---------|------|------|---------|
| `POST /user/login` | `rate:login:{ip}` | 5次 | 60s | IP |
| `POST /admin/login` | `rate:admin-login:{ip}` | 5次 | 60s | IP |
| `POST /user/register` | `rate:register:{ip}` | 3次 | 60s | IP |
| `POST /user/send-code` | `rate:code:{phone}` | 1次 | 60s | 请求参数 |
| `POST /user/orders/create` | `rate:submit-order:{userId}` | 3次 | 10s | 用户ID |

实现：`@RateLimit` 注解 + `RateLimitInterceptor`（`common` 模块，固定窗口计数）

### 2.2 登录保护

| 用途 | Key | TTL | 逻辑 |
|------|-----|-----|------|
| 用户登录失败计数 | `login:fail:{ip}` | 15min | 失败 +1，满 5 次锁定 15min，成功后删除 |
| 管理员登录失败计数 | `login:fail:admin:{username}` | 15min | 同上（按用户名计数） |

### 2.3 短信验证码

| 用途 | Key | TTL | 操作 |
|------|-----|-----|------|
| 存储验证码 | `verification:code:{phone}` | 5min | set → get(校验) → delete(注册成功后移除) |

### 2.4 幂等性

| 场景 | Key | TTL | 说明 |
|------|-----|-----|------|
| 订单创建 MQ | `idempotent:order:create:{orderNumber}` | 5min | 防 MQ 重复消费 |
| 订单支付 MQ | `idempotent:order:pay:{orderId}` | 5min | 同上 |
| 订单取消 MQ | `idempotent:order:cancel:{orderId}` | 5min | 同上 |
| 退款 MQ | `idempotent:refund:{refundId}` | 24h | 退款窗口较长 |
| 防重复下单 Token | `idempotent:submit-order:{token}` | 5min | 先 reserve 后 consume |

实现：`IdempotentUtil`（SET NX + DEL）

---

## 三、已定义但未使用的工具

| 工具 | 文件 | 说明 |
|------|------|------|
| `RedisLockUtil` | `common/.../utils/RedisLockUtil.java` | 分布式锁（SET NX PX + Lua 解锁），含 `acquireWithRetry` 自旋方法 |

---

## 四、各服务 Redis 使用情况

| 服务 | 端口 | 状态 |
|------|------|------|
| gateway | 8080 | ✅ 登录保护 + 验证码 |
| mailadmin | 8081 | ✅ 商品/分类缓存失效 + 优惠券/评价缓存失效 |
| user-service | 8083 | ✅ 用户信息缓存 |
| cart-service | 8084 | ✅ 商品/分类缓存读写 |
| order-service | 8085 | ✅ 商品/分类缓存读写 + 库存缓存 + 订单统计缓存 + 幂等 |
| coupon-service | 8086 | ✅ 优惠券缓存读写 |
| refund-service | 8087 | ❌ 仅有连接配置，无 Redis 代码 |
| review-service | 8088 | ✅ 评价缓存读写 |

---

## 五、建议新增缓存（未实现）

| 优先级 | 数据 | 建议 Key | 建议 TTL | 失效策略 |
|--------|------|---------|---------|---------|
| 中 | 订单详情 | `order:detail:{orderId}` | 10min | 订单状态变更时 DELETE |
| 中 | 用户余额 | `user:balance:{userId}` | 1min | 充值/支付/退款时 DELETE，短 TTL 兜底 |
| 中 | 搜索热词 | `search:hotKeywords` | 1h | 异步定时统计更新 |

---

## 六、Cache-Aside 通用模式

```
读取：查缓存 → hit 返回 → miss 查 DB → 写缓存 + 设 TTL → 返回
写入：写 DB → DELETE 缓存 key
下次读取时自然 miss，从 DB 加载最新数据回填缓存
```

所有业务数据缓存均遵循此模式，确保数据一致性。
