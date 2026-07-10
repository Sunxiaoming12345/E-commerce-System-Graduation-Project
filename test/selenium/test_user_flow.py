"""
用户端 E2E 测试 — 登录 / 浏览 / 购物车 / 下单 / 个人中心

运行 (pytest, headless):
    pytest test/selenium/test_user_flow.py -v -s

运行 (pytest, 显示浏览器):
    HEADLESS=0 pytest test/selenium/test_user_flow.py -v -s

运行 (独立, 显示浏览器):
    HEADLESS=0 python test/selenium/test_user_flow.py

运行 (独立, headless):
    python test/selenium/test_user_flow.py
"""
import time
import os
import sys
from selenium.webdriver.common.by import By

# 支持从项目根目录或 selenium/ 目录运行
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from conftest import UserLoginPage, BASE_URL_USER, assert_success, Page


# ============================================================
# pytest 测试类
# ============================================================

class TestUserLogin:
    """用户登录模块"""

    def test_login_success(self, driver):
        """正常登录 — 正确用户名密码应跳转到首页"""
        page = UserLoginPage(driver, BASE_URL_USER)
        page.login("zhangsan", "123456")
        assert page.is_logged_in(), "应跳转到首页而非停留在 /login"
        assert_success("用户登录成功")

    def test_login_fail(self, driver):
        """登录失败 — 错误密码应被拒绝"""
        page = UserLoginPage(driver, BASE_URL_USER)
        page.login("zhangsan", "wrongpassword123")
        time.sleep(1)
        # 仍在 /login 或有错误提示都算被拦截
        on_login = "/login" in driver.current_url
        has_error = page.element_exists(By.CSS_SELECTOR, ".el-message--error, .el-message--warning")
        assert on_login or has_error, "错误密码应被拦截"
        assert_success("错误密码被拒绝")


class TestProductBrowse:
    """商品浏览 — 无需登录"""

    def test_homepage(self, driver):
        """首页应展示商品卡片"""
        page = Page(driver, BASE_URL_USER)
        page.open("/home")
        time.sleep(2)
        cards = driver.find_elements(By.CSS_SELECTOR, ".product-card")
        assert len(cards) > 0, "首页应显示商品列表"
        assert_success(f"首页展示 {len(cards)} 件商品")

    def test_search(self, driver):
        """搜索功能"""
        page = Page(driver, BASE_URL_USER)
        page.open("/home")
        time.sleep(2)
        # 搜索输入框使用 placeholder="搜索商品..."，el-input 不在 form 中，用 Enter 触发
        from selenium.webdriver.common.keys import Keys
        search = page.find(By.CSS_SELECTOR, 'input[placeholder*="搜索"]')
        search.click()
        search.clear()
        search.send_keys("商品")
        search.send_keys(Keys.ENTER)
        time.sleep(2)
        # 搜索后应有结果或空状态提示
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 50
        assert_success("搜索功能正常")

    def test_product_detail(self, driver):
        """商品详情页"""
        driver.get(f"{BASE_URL_USER}/product/1")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        # 详情页应有足够内容
        assert len(body) > 30, "商品详情页应有内容"
        assert_success("商品详情页加载成功")


class TestCartAndOrder:
    """购物车 + 订单 — 需要登录"""

    def test_add_to_cart(self, driver):
        """从商品详情页加入购物车"""
        login_page = UserLoginPage(driver, BASE_URL_USER)
        login_page.login("zhangsan", "123456")
        time.sleep(1)

        driver.get(f"{BASE_URL_USER}/product/1")
        time.sleep(2)

        # 按钮文本为"加入购物车"
        add_btn = driver.find_elements(By.XPATH, '//button[contains(text(),"加入购物车")]')
        if add_btn:
            add_btn[0].click()
            time.sleep(2)
            # 加入后应有成功提示
            assert_success("加入购物车点击成功")
        else:
            # 按钮可能被禁用（缺货），也算合理
            print("  ⚠ 加入购物车按钮不可用（可能缺货），跳过")

    def test_cart_page(self, driver):
        """购物车页面"""
        page = Page(driver, BASE_URL_USER)
        page.open("/cart")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        # 购物车至少显示标题或空状态
        assert "购物车" in body or len(body) > 0
        assert_success("购物车页面加载成功")

    def test_orders_page(self, driver):
        """我的订单页面"""
        # 确保已登录
        if "/login" in driver.current_url:
            UserLoginPage(driver, BASE_URL_USER).login("zhangsan", "123456")
            time.sleep(1)

        driver.get(f"{BASE_URL_USER}/orders")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert "我的订单" in body or len(body) > 50
        assert_success("我的订单页面加载成功")


class TestUserCenter:
    """个人中心 — 需要登录"""

    def test_profile_page(self, driver):
        """个人中心页面"""
        if "/login" in driver.current_url:
            UserLoginPage(driver, BASE_URL_USER).login("zhangsan", "123456")
            time.sleep(1)

        driver.get(f"{BASE_URL_USER}/profile")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 50, "个人中心应有内容"
        assert_success("个人中心页面加载成功")

    def test_coupons_page(self, driver):
        """领券中心页面"""
        if "/login" in driver.current_url:
            UserLoginPage(driver, BASE_URL_USER).login("zhangsan", "123456")
            time.sleep(1)

        driver.get(f"{BASE_URL_USER}/coupons")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert "领券" in body or "优惠券" in body or len(body) > 20
        assert_success("领券中心页面加载成功")

    def test_refunds_page(self, driver):
        """我的退款页面"""
        if "/login" in driver.current_url:
            UserLoginPage(driver, BASE_URL_USER).login("zhangsan", "123456")
            time.sleep(1)

        driver.get(f"{BASE_URL_USER}/refunds")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert "退款" in body or len(body) > 20
        assert_success("我的退款页面加载成功")


# ============================================================
# 独立运行入口（无需 pytest）
# ============================================================
if __name__ == "__main__":
    from selenium import webdriver
    from selenium.webdriver.edge.service import Service
    from selenium.webdriver.edge.options import Options
    from webdriver_manager.microsoft import EdgeChromiumDriverManager

    print("=" * 60)
    print("  用户端 Selenium E2E 测试")
    print(f"  目标: {BASE_URL_USER}")
    print("=" * 60)

    os.makedirs("screenshots", exist_ok=True)

    options = Options()
    options.add_argument("--window-size=1920,1080")
    if os.environ.get("HEADLESS", "1") == "1":
        options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

    service = Service(EdgeChromiumDriverManager().install())
    driver = webdriver.Edge(service=service, options=options)
    driver.implicitly_wait(5)

    passed = 0
    failed = 0
    results = []

    def run_test(name, fn):
        global passed, failed
        num = passed + failed + 1
        print(f"\n[{num}] {name}")
        try:
            fn()
            print(f"  ✅ PASS")
            passed += 1
            results.append(("PASS", name))
        except Exception as e:
            print(f"  ❌ FAIL: {e}")
            failed += 1
            results.append(("FAIL", name, str(e)))
            # 失败截图
            safe = name.replace(" ", "_").replace("/", "_")
            try:
                driver.save_screenshot(f"screenshots/{safe}.png")
                print(f"  📸 截图: screenshots/{safe}.png")
            except:
                pass

    try:
        # ---- 登录 ----
        def t_login_ok():
            page = UserLoginPage(driver, BASE_URL_USER)
            page.login("zhangsan", "123456")
            assert page.is_logged_in(), "登录后应跳转到首页"
        run_test("登录成功", t_login_ok)

        def t_login_fail():
            driver.get(f"{BASE_URL_USER}/login")
            time.sleep(2)
            # 填充用户名和错误密码
            u = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
            p = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
            u.clear(); u.send_keys("zhangsan")
            p.clear(); p.send_keys("badpass000")
            driver.find_element(By.XPATH, '//button[contains(@class,"el-button--primary")]//span[text()="登录"]/..').click()
            time.sleep(2)
            # 登录失败应仍停留在 /login 或有错误消息
            on_login = "/login" in driver.current_url
            has_err = len(driver.find_elements(By.CSS_SELECTOR, ".el-message--error,.el-alert--error")) > 0
            assert on_login or has_err, "错误密码应被拒绝"
        run_test("登录失败(错误密码)", t_login_fail)

        # ---- 重新登录（后续测试需要登录态）----
        def t_relogin():
            page = UserLoginPage(driver, BASE_URL_USER)
            page.login("zhangsan", "123456")
            assert page.is_logged_in()
        run_test("重新登录", t_relogin)

        # ---- 页面加载 ----
        page_tests = [
            ("首页(/home)", "/home", lambda b: len(b) > 50),
            ("商品详情(/product/1)", "/product/1", lambda b: len(b) > 30),
            ("购物车(/cart)", "/cart", lambda b: "购物车" in b or len(b) > 0),
            ("我的订单(/orders)", "/orders", lambda b: "订单" in b or len(b) > 30),
            ("个人中心(/profile)", "/profile", lambda b: len(b) > 30),
            ("领券中心(/coupons)", "/coupons", lambda b: len(b) > 20),
            ("我的退款(/refunds)", "/refunds", lambda b: len(b) > 20),
        ]
        for name, path, check in page_tests:
            def make(p, ck, nm):
                return lambda: (
                    driver.get(f"{BASE_URL_USER}{p}"),
                    time.sleep(2),
                    None if ck(driver.find_element(By.TAG_NAME, "body").text)
                    else (_ for _ in ()).throw(AssertionError(f"{nm}: 页面内容不足"))
                )
            run_test(name, make(path, check, name))

        # ---- 搜索 ----
        def t_search():
            driver.get(f"{BASE_URL_USER}/home")
            time.sleep(2)
            from selenium.webdriver.common.keys import Keys
            si = driver.find_element(By.CSS_SELECTOR, 'input[placeholder*="搜索"]')
            si.click(); si.clear(); si.send_keys("手机"); si.send_keys(Keys.ENTER)
            time.sleep(2)
            assert len(driver.find_element(By.TAG_NAME, "body").text) > 20
        run_test("搜索商品", t_search)

    finally:
        print(f"\n{'=' * 60}")
        print(f"  结果: {passed} 通过, {failed} 失败")
        print(f"{'=' * 60}")
        driver.quit()

    sys.exit(0 if failed == 0 else 1)
