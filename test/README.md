# 测试套件

## 目录结构

```
test/
├── README.md
├── run_all.sh                    # 一键运行全部测试
├── requirements.txt              # Python 依赖
├── shell/
│   ├── concurrent_test.sh        # 并发原子性测试（库存、余额、优惠券）
│   ├── api_smoke_test.sh         # API 冒烟测试（接口正常/异常/安全）
│   └── data_consistency.sh       # 数据一致性测试（下单→支付 链路验证）
└── selenium/
    ├── conftest.py               # 公共配置、Page Objects、Fixtures
    ├── test_user_flow.py         # 用户端 E2E（登录→浏览→购物车→下单→支付）
    └── test_admin_flow.py        # 管理端 E2E（登录→订单→商品→退款审批）
```

## 快速开始

### 安装依赖

```bash
pip install -r test/requirements.txt
```

### 运行全部测试

```bash
bash test/run_all.sh
```

### 按类型运行

```bash
# 仅 Shell 测试（并发 + API + 数据一致性）
bash test/run_all.sh shell

# 仅 Selenium 测试（UI 端到端）
bash test/run_all.sh selenium
```

### 单独运行

```bash
# Shell 测试
bash test/shell/concurrent_test.sh
bash test/shell/api_smoke_test.sh
bash test/shell/data_consistency.sh

# Selenium 测试（独立模式，无需 pytest）
python test/selenium/test_user_flow.py
python test/selenium/test_admin_flow.py

# Selenium 测试（pytest 模式，可选中具体用例）
pytest test/selenium/test_user_flow.py -v -s
pytest test/selenium/test_admin_flow.py -v -s
pytest test/selenium/test_user_flow.py::TestUserLogin -v -s
```

### 调试模式（显示浏览器界面）

```bash
HEADLESS=0 python test/selenium/test_user_flow.py
```

## 测试覆盖矩阵

| 测试类型 | 工具 | 覆盖内容 |
|---------|------|---------|
| 并发 — 库存防超卖 | MySQL 直连 | `UPDATE WHERE stock >= ?` 原子性 |
| 并发 — 余额防超额 | MySQL 直连 | `UPDATE WHERE balance >= ?` 原子性 |
| 并发 — 优惠券防超领 | MySQL 直连 | `WHERE used_count < total_count` |
| API — 登录认证 | curl | 正常登录、错误密码、不存在用户、Token 401 |
| API — 商品浏览 | curl | 推荐、分类、搜索、详情 |
| API — 认证接口 | curl | 购物车、订单、余额、个人信息 |
| API — 权限控制 | curl | 用户 Token 访问管理接口被拒 |
| API — 安全性 | curl | SQL 注入无害处理 |
| API — 频率限制 | curl | 登录 6 次/60s 触发限流 |
| 数据一致性 | curl + MySQL | 下单前后库存/订单/支付记录验证 |
| UI — 用户端页面 | Selenium | 登录→首页→商品→购物车→订单→个人中心→优惠券 |
| UI — 管理端页面 | Selenium | 登录→工作台→商品→订单→分类→支付→评价→优惠券→退款 |

### 测试账号

| 角色 | 用户名 | 密码 | 端口 |
|------|--------|------|------|
| 普通用户 | zhangsan | 123456 | 80 |
| 管理员 | sunxiaoming | 123456 | 81 |

## 前提条件

- **Shell 测试**: MySQL 客户端可连接 `192.168.100.128:3306`，curl 可达 `192.168.100.128:80/81`
- **Selenium 测试**: Windows 自带 Edge 浏览器，`webdriver-manager` 自动下载驱动
- **所有测试**: 虚拟机服务正常运行
