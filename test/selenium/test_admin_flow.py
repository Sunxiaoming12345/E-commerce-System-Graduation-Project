"""
管理端 E2E 测试 — 登录 / 工作台 / 商品 / 订单 / 分类 / 支付 / 评价 / 优惠券 / 退款

运行 (pytest, headless):
    pytest test/selenium/test_admin_flow.py -v -s

运行 (pytest, 显示浏览器):
    HEADLESS=0 pytest test/selenium/test_admin_flow.py -v -s

运行 (独立, 显示浏览器):
    HEADLESS=0 python test/selenium/test_admin_flow.py

运行 (独立, headless):
    python test/selenium/test_admin_flow.py
"""
import time
import os
import sys
from selenium.webdriver.common.by import By

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from conftest import AdminLoginPage, BASE_URL_ADMIN, assert_success


# ============================================================
# pytest 测试类
# ============================================================

class TestAdminLogin:
    """管理端登录模块"""

    def test_login_success(self, driver):
        """正常登录"""
        page = AdminLoginPage(driver, BASE_URL_ADMIN)
        page.login("sunxiaoming", "123456")
        assert page.is_logged_in(), "登录成功后应跳转离开 /login"
        assert_success("管理端登录成功")

    def test_login_fail(self, driver):
        """错误密码"""
        page = AdminLoginPage(driver, BASE_URL_ADMIN)
        page.login("admin", "wrongpassword")
        time.sleep(1)
        on_login = "/login" in driver.current_url
        has_err = page.element_exists(By.CSS_SELECTOR, ".el-message--error, .el-alert--error, .el-form-item__error")
        assert on_login or has_err, "错误密码应被拒绝"
        assert_success("错误密码被拒绝")

    def test_unauthorized_redirect(self, driver):
        """未登录访问受保护页 — 应重定向到 /login"""
        driver.get(f"{BASE_URL_ADMIN}/dashboard")
        time.sleep(2)
        from_login = "/login" in driver.current_url
        has_input = len(driver.find_elements(By.CSS_SELECTOR, 'input')) > 0
        assert from_login or has_input, "未登录应看到登录页"
        assert_success("未登录用户被重定向到登录页")


class TestAdminPages:
    """管理端所有页面加载"""

    def _ensure_login(self, driver):
        """确保已登录（跳过 /login 则无需重复登录）"""
        if "/login" in driver.current_url:
            AdminLoginPage(driver, BASE_URL_ADMIN).login("sunxiaoming", "123456")
            time.sleep(1)

    def test_dashboard(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/dashboard")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert "工作台" in body or len(body) > 50
        assert_success("工作台加载成功")

    def test_products_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/products")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 20
        assert_success("商品管理页加载成功")

    def test_orders_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/orders")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 20
        assert_success("订单管理页加载成功")

    def test_categories_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/categories")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 10
        assert_success("分类管理页加载成功")

    def test_payments_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/payments")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 10
        assert_success("支付记录页加载成功")

    def test_reviews_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/reviews")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 10
        assert_success("评价管理页加载成功")

    def test_coupons_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/coupons")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 10
        assert_success("优惠券管理页加载成功")

    def test_refunds_page(self, driver):
        self._ensure_login(driver)
        driver.get(f"{BASE_URL_ADMIN}/refunds")
        time.sleep(2)
        body = driver.find_element(By.TAG_NAME, "body").text
        assert len(body) > 10
        assert_success("退款管理页加载成功")


# ============================================================
# 独立运行入口（无需 pytest）
# ============================================================
if __name__ == "__main__":
    from selenium import webdriver
    from selenium.webdriver.edge.service import Service
    from selenium.webdriver.edge.options import Options
    from webdriver_manager.microsoft import EdgeChromiumDriverManager

    print("=" * 60)
    print("  管理端 Selenium E2E 测试")
    print(f"  目标: {BASE_URL_ADMIN}")
    print("=" * 60)

    os.makedirs("screenshots", exist_ok=True)

    options = Options()
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

    service = Service(EdgeChromiumDriverManager().install())
    driver = webdriver.Edge(service=service, options=options)
    driver.implicitly_wait(5)

    passed = 0
    failed = 0

    def run_test(name, fn):
        global passed, failed
        num = passed + failed + 1
        print(f"\n[{num}] {name}")
        try:
            fn()
            print(f"  ✅ PASS")
            passed += 1
        except Exception as e:
            print(f"  ❌ FAIL: {e}")
            failed += 1
            safe = "admin_" + name.replace(" ", "_").replace("/", "_")
            try:
                driver.save_screenshot(f"screenshots/{safe}.png")
                print(f"  📸 截图: screenshots/{safe}.png")
            except:
                pass

    try:
        # ---- 登录 ----
        def t_login_ok():
            page = AdminLoginPage(driver, BASE_URL_ADMIN)
            page.login("sunxiaoming", "123456")
            assert page.is_logged_in(), "登录后应跳转离开 /login"
        run_test("管理员登录成功", t_login_ok)

        # ---- 全部 8 个管理页面 ----
        admin_pages = [
            "dashboard", "products", "orders", "categories",
            "payments", "reviews", "coupons", "refunds",
        ]
        for name in admin_pages:
            def make(p):
                return lambda: (
                    driver.get(f"{BASE_URL_ADMIN}/{p}"),
                    time.sleep(2),
                    None if len(driver.find_element(By.TAG_NAME, "body").text) > 10
                    else (_ for _ in ()).throw(AssertionError(f"{p}: 页面内容不足"))
                )
            run_test(f"{name} 页面", make(name))

        # ---- 登录失败 ----
        def t_login_fail():
            driver.get(f"{BASE_URL_ADMIN}/login")
            time.sleep(2)
            u = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
            p = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
            u.clear(); u.send_keys("admin")
            p.clear(); p.send_keys("wrongpassword")
            driver.find_element(By.CSS_SELECTOR, "button.submit-btn").click()
            time.sleep(2)
            on_login = "/login" in driver.current_url
            has_err = len(driver.find_elements(By.CSS_SELECTOR, ".el-message--error,.el-alert--error")) > 0
            assert on_login or has_err, "错误密码应被拒绝"
        run_test("管理员登录失败", t_login_fail)

        # ---- 未登录重定向 ----
        def t_unauth():
            driver.get(f"{BASE_URL_ADMIN}/dashboard")
            time.sleep(2)
            on_login = "/login" in driver.current_url
            has_input = len(driver.find_elements(By.CSS_SELECTOR, "input")) > 0
            assert on_login or has_input, "未登录应看到登录页"
        run_test("未登录重定向", t_unauth)

    finally:
        print(f"\n{'=' * 60}")
        print(f"  结果: {passed} 通过, {failed} 失败")
        print(f"{'=' * 60}")
        driver.quit()

    sys.exit(0 if failed == 0 else 1)
