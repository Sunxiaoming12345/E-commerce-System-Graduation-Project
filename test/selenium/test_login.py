"""
登录模块专项测试
用法:
    HEADLESS=0 python test/selenium/test_login.py user    # 仅用户端
    HEADLESS=0 python test/selenium/test_login.py admin   # 仅管理端
    HEADLESS=0 python test/selenium/test_login.py all     # 全部 (默认)
"""
import time, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from selenium import webdriver
from selenium.webdriver.edge.service import Service
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By
from webdriver_manager.microsoft import EdgeChromiumDriverManager

# ============================================================
# 配置
# ============================================================
BASE_URL_USER  = os.environ.get("TEST_USER_URL",  "http://192.168.100.128:80")
BASE_URL_ADMIN = os.environ.get("TEST_ADMIN_URL", "http://192.168.100.128:81")

# 正确账号
USER  = ("zhangsan",     "123456")
ADMIN = ("sunxiaoming",  "123456")

# ============================================================
# 工具
# ============================================================
def screenshot(driver, name):
    os.makedirs("screenshots", exist_ok=True)
    driver.save_screenshot(f"screenshots/login_{name}.png")

def run(name, fn):
    print(f"\n{'─' * 50}")
    print(f"  [{name}]")
    try:
        fn()
        print(f"  ✅ PASS")
        return True
    except Exception as e:
        print(f"  ❌ FAIL — {e}")
        return False

# ============================================================
# 用户端登录测试
# ============================================================
def test_user_login_success(driver):
    """正确账号密码 → 跳转到首页"""
    driver.get(f"{BASE_URL_USER}/login")
    time.sleep(2)

    # 定位输入框
    username_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
    password_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
    # 定位登录按钮
    login_btn = driver.find_element(By.XPATH, '//button[contains(@class,"el-button--primary")]//span[text()="登录"]/..')

    username_inp.clear(); username_inp.send_keys(USER[0])
    password_inp.clear(); password_inp.send_keys(USER[1])
    login_btn.click()
    time.sleep(2)

    assert "/login" not in driver.current_url, f"登录后应跳转，但仍停留在 {driver.current_url}"


def test_user_login_fail(driver):
    """错误密码 → 被拒绝"""
    driver.get(f"{BASE_URL_USER}/login")
    time.sleep(2)

    username_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
    password_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
    login_btn = driver.find_element(By.XPATH, '//button[contains(@class,"el-button--primary")]//span[text()="登录"]/..')

    username_inp.clear(); username_inp.send_keys(USER[0])
    password_inp.clear(); password_inp.send_keys("wrongpassword999")
    login_btn.click()
    time.sleep(2)

    # 仍在登录页 或 出现错误提示
    on_login  = "/login" in driver.current_url
    has_error = len(driver.find_elements(By.CSS_SELECTOR, ".el-message--error")) > 0
    assert on_login or has_error, "错误密码应该被拒绝"


def test_user_empty_fields(driver):
    """空表单提交 → 前端校验拦截"""
    driver.get(f"{BASE_URL_USER}/login")
    time.sleep(2)

    login_btn = driver.find_element(By.XPATH, '//button[contains(@class,"el-button--primary")]//span[text()="登录"]/..')
    login_btn.click()
    time.sleep(2)

    # 应仍在 /login 或有表单校验错误
    on_login  = "/login" in driver.current_url
    has_error = len(driver.find_elements(By.CSS_SELECTOR, ".el-form-item__error")) > 0
    assert on_login or has_error, "空表单应有校验拦截"


# ============================================================
# 管理端登录测试
# ============================================================
def test_admin_login_success(driver):
    """正确账号密码 → 跳转到工作台"""
    driver.get(f"{BASE_URL_ADMIN}/login")
    time.sleep(2)

    username_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
    password_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
    login_btn = driver.find_element(By.CSS_SELECTOR, "button.submit-btn")

    username_inp.clear(); username_inp.send_keys(ADMIN[0])
    password_inp.clear(); password_inp.send_keys(ADMIN[1])
    login_btn.click()
    time.sleep(2)

    assert "/login" not in driver.current_url, f"登录后应跳转，但仍停留在 {driver.current_url}"


def test_admin_login_fail(driver):
    """错误密码 → 被拒绝"""
    driver.get(f"{BASE_URL_ADMIN}/login")
    time.sleep(2)

    username_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="用户名"]')
    password_inp = driver.find_element(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]')
    login_btn = driver.find_element(By.CSS_SELECTOR, "button.submit-btn")

    username_inp.clear(); username_inp.send_keys(ADMIN[0])
    password_inp.clear(); password_inp.send_keys("wrongpassword999")
    login_btn.click()
    time.sleep(2)

    on_login  = "/login" in driver.current_url
    has_error = len(driver.find_elements(By.CSS_SELECTOR, ".el-message--error")) > 0
    assert on_login or has_error, "错误密码应该被拒绝"


def test_admin_empty_fields(driver):
    """空表单提交 → 前端校验拦截"""
    driver.get(f"{BASE_URL_ADMIN}/login")
    time.sleep(2)

    login_btn = driver.find_element(By.CSS_SELECTOR, "button.submit-btn")
    login_btn.click()
    time.sleep(2)

    on_login  = "/login" in driver.current_url
    has_error = len(driver.find_elements(By.CSS_SELECTOR, ".el-form-item__error")) > 0
    assert on_login or has_error, "空表单应有校验拦截"


# ============================================================
# Main
# ============================================================
if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else "all"

    print("=" * 56)
    print("  登录模块 E2E 测试")
    print(f"  用户端: {BASE_URL_USER} | 管理端: {BASE_URL_ADMIN}")
    print("=" * 56)

    options = Options()
    options.add_argument("--window-size=1920,1080")
    if os.environ.get("HEADLESS") == "1":
        options.add_argument("--headless")
        print("  模式: headless (无界面)")
    else:
        print("  模式: 显示浏览器")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

    service = Service(EdgeChromiumDriverManager().install())
    driver = webdriver.Edge(service=service, options=options)
    driver.implicitly_wait(5)

    passed = 0
    failed = 0

    # --- 用户端 ---
    if target in ("user", "all"):
        tests = [
            ("用户端 - 登录成功",        test_user_login_success),
            ("用户端 - 错误密码被拒",    test_user_login_fail),
            ("用户端 - 空表单校验",      test_user_empty_fields),
        ]
        for name, fn in tests:
            if run(name, lambda fn=fn: fn(driver)):
                passed += 1
            else:
                failed += 1
                screenshot(driver, name.replace(" ", "_"))

    # --- 管理端 ---
    if target in ("admin", "all"):
        tests = [
            ("管理端 - 登录成功",        test_admin_login_success),
            ("管理端 - 错误密码被拒",    test_admin_login_fail),
            ("管理端 - 空表单校验",      test_admin_empty_fields),
        ]
        for name, fn in tests:
            if run(name, lambda fn=fn: fn(driver)):
                passed += 1
            else:
                failed += 1
                screenshot(driver, name.replace(" ", "_"))

    driver.quit()

    print(f"\n{'=' * 56}")
    print(f"  结果: {passed} 通过, {failed} 失败 (共 {passed+failed})")
    print(f"{'=' * 56}")
    sys.exit(0 if failed == 0 else 1)
