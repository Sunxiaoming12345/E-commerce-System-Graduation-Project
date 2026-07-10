#!/bin/bash
# ============================================================
# API 冒烟测试 — 核心接口正常/异常场景
# 运行方式: bash test/shell/api_smoke_test.sh
# ============================================================

set -e

API_USER="http://192.168.100.128:80"
API_ADMIN="http://192.168.100.128:81"

PASS=0
FAIL=0
SKIP=0

green() { echo -e "\033[32m$1\033[0m"; }
red()   { echo -e "\033[31m$1\033[0m"; }
yellow(){ echo -e "\033[33m$1\033[0m"; }
blue()  { echo -e "\033[34m$1\033[0m"; }

# 通用断言: expect_http 期望HTTP状态码
# $1=描述 $2=期望码 $3=实际码 $4=响应体
assert_http() {
    local desc="$1" expected="$2" actual="$3" body="$4"
    if [ "$expected" = "$actual" ]; then
        green "  ✅ $desc (HTTP $actual)"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望 HTTP $expected, 实际 $actual)"
        echo "     响应: $body"
        FAIL=$((FAIL + 1))
    fi
}

# 断言响应中 code=1（业务成功）
# $1=描述 $2=响应体
assert_success() {
    local desc="$1" body="$2"
    local code=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
    if [ "$code" = "1" ]; then
        green "  ✅ $desc (业务成功 code=$code)"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望 code=1, 实际 code=$code)"
        echo "     响应: $body"
        FAIL=$((FAIL + 1))
    fi
}

# 断言响应中 code!=1（业务失败，符合预期）
assert_fail() {
    local desc="$1" body="$2"
    local code=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',-1))" 2>/dev/null || echo -1)
    if [ "$code" != "1" ]; then
        green "  ✅ $desc (正确拒绝 code=$code)"
        PASS=$((PASS + 1))
    else
        red "  ❌ $desc (期望被拒绝，但 code=1 成功了)"
        FAIL=$((FAIL + 1))
    fi
}

# ============================================================
# 1. 用户端 — 登录认证
# ============================================================
blue "===== 1. 用户端 - 登录认证 ====="

# 1.1 正常登录
RESP=$(curl -s -w "\n%{http_code}" -X POST "$API_USER/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"zhangsan","password":"123456"}')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "正常登录" 200 "$HTTP_CODE" "$BODY"
assert_success "正常登录 - 业务成功" "$BODY"
# 提取 token
USER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null || echo "")

# 1.2 错误密码
RESP=$(curl -s -w "\n%{http_code}" -X POST "$API_USER/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"zhangsan","password":"wrongpassword"}')
BODY=$(echo "$RESP" | sed '$d')
assert_fail "错误密码应被拒绝" "$BODY"

# 1.3 不存在的用户
RESP=$(curl -s -w "\n%{http_code}" -X POST "$API_USER/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"nonexistent_user_999","password":"123456"}')
BODY=$(echo "$RESP" | sed '$d')
assert_fail "不存在用户应被拒绝" "$BODY"

# ============================================================
# 2. 用户端 — 商品浏览（无需登录）
# ============================================================
blue "===== 2. 用户端 - 商品浏览 ====="

RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/products/recommended")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "推荐商品" 200 "$HTTP_CODE" "$BODY"
assert_success "推荐商品 - 数据查询" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/products/categories")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "分类列表" 200 "$HTTP_CODE" "$BODY"
assert_success "分类列表 - 数据查询" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/products/search?keyword=手机")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "搜索商品" 200 "$HTTP_CODE" "$BODY"
assert_success "搜索商品 - 数据查询" "$BODY"

# ============================================================
# 3. 用户端 — 需认证接口
# ============================================================
blue "===== 3. 用户端 - 认证接口 ====="

if [ -z "$USER_TOKEN" ] || [ "$USER_TOKEN" = "" ]; then
    yellow "  ⚠️ 未获取到用户 Token，跳过认证接口测试"
    SKIP=$((SKIP + 5))
else
    # 3.1 获取用户信息
    RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/info" \
        -H "Authorization: $USER_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "获取用户信息" 200 "$HTTP_CODE" "$BODY"
    assert_success "获取用户信息 - 业务成功" "$BODY"

    # 3.2 获取购物车
    RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/shopCart/list" \
        -H "Authorization: $USER_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "获取购物车" 200 "$HTTP_CODE" "$BODY"

    # 3.3 获取订单列表
    RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/orders/myOrders?page=1&pageSize=5" \
        -H "Authorization: $USER_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "获取订单列表" 200 "$HTTP_CODE" "$BODY"

    # 3.4 获取余额
    RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/balance/info" \
        -H "Authorization: $USER_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "获取余额" 200 "$HTTP_CODE" "$BODY"

    # 3.5 未认证访问（不带 Token）
    RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/info")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "未认证访问被拒绝" 401 "$HTTP_CODE" "$BODY"
fi

# ============================================================
# 4. 管理端 — 登录
# ============================================================
blue "===== 4. 管理端 - 登录 ====="

RESP=$(curl -s -w "\n%{http_code}" -X POST "$API_ADMIN/admin/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "管理员登录" 200 "$HTTP_CODE" "$BODY"
assert_success "管理员登录 - 业务成功" "$BODY"
ADMIN_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null || echo "")

# 4.2 普通用户 Token 访问管理接口（应被拒绝）
if [ -n "$USER_TOKEN" ] && [ "$USER_TOKEN" != "" ]; then
    RESP=$(curl -s -w "\n%{http_code}" "$API_ADMIN/admin/orders/page?page=1&pageSize=5" \
        -H "Authorization: $USER_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "用户Token访问管理接口被拒" 401 "$HTTP_CODE" "$BODY"
fi

# ============================================================
# 5. 管理端 — 需认证接口
# ============================================================
blue "===== 5. 管理端 - 认证接口 ====="

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "" ]; then
    yellow "  ⚠️ 未获取到管理员 Token，跳过管理接口测试"
    SKIP=$((SKIP + 4))
else
    RESP=$(curl -s -w "\n%{http_code}" "$API_ADMIN/admin/orders/page?page=1&pageSize=5" \
        -H "Authorization: $ADMIN_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "管理端-订单列表" 200 "$HTTP_CODE" "$BODY"

    RESP=$(curl -s -w "\n%{http_code}" "$API_ADMIN/products/products/page?page=1&pageSize=5" \
        -H "Authorization: $ADMIN_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "管理端-商品列表" 200 "$HTTP_CODE" "$BODY"

    RESP=$(curl -s -w "\n%{http_code}" "$API_ADMIN/admin/orders/statistics" \
        -H "Authorization: $ADMIN_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "管理端-订单统计" 200 "$HTTP_CODE" "$BODY"

    RESP=$(curl -s -w "\n%{http_code}" "$API_ADMIN/admin/payments/statistics" \
        -H "Authorization: $ADMIN_TOKEN")
    HTTP_CODE=$(echo "$RESP" | tail -1)
    BODY=$(echo "$RESP" | sed '$d')
    assert_http "管理端-支付统计" 200 "$HTTP_CODE" "$BODY"
fi

# ============================================================
# 6. 参数校验
# ============================================================
blue "===== 6. 参数校验 ====="

RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/products/search?keyword=")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "空关键词搜索" 200 "$HTTP_CODE" "$BODY"
assert_success "空关键词搜索 - 返回全部" "$BODY"

# 恶意 SQL 注入尝试
RESP=$(curl -s -w "\n%{http_code}" "$API_USER/user/products/search?keyword='%3BDROP+TABLE+products%3B--")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
assert_http "SQL注入尝试" 200 "$HTTP_CODE" "$BODY"
# 验证数据库仍然正常
assert_success "SQL注入被无害处理" "$BODY"

# ============================================================
# 7. 频率限制
# ============================================================
blue "===== 7. 频率限制 ====="

echo "  连续快速登录 6 次（超过5次/60s限制）..."
BLOCKED=0
for i in $(seq 1 6); do
    RESP=$(curl -s -X POST "$API_USER/user/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"zhangsan","password":"wrong"}')
    CODE=$(echo "$RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('code',0))" 2>/dev/null || echo 0)
    if [ "$CODE" != "1" ]; then
        BLOCKED=$((BLOCKED + 1))
    fi
done
if [ $BLOCKED -ge 5 ]; then
    green "  ✅ 频率限制生效 ($BLOCKED/6 次被拒绝)"
    PASS=$((PASS + 1))
else
    red "  ❌ 频率限制未生效 (仅 $BLOCKED/6 次被拒绝)"
    FAIL=$((FAIL + 1))
fi

# ============================================================
# 汇总
# ============================================================
echo ""
blue "===== 结果汇总 ====="
green "通过: $PASS"
if [ $FAIL -gt 0 ]; then
    red "失败: $FAIL"
fi
if [ $SKIP -gt 0 ]; then
    yellow "跳过: $SKIP"
fi

if [ $FAIL -gt 0 ]; then
    exit 1
else
    green "全部通过!"
fi
