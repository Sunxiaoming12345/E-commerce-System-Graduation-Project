"""
共享配置、页面对象和 Fixtures
"""
import os
import time
import pytest
from selenium import webdriver
from selenium.webdriver.edge.service import Service
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.microsoft import EdgeChromiumDriverManager

BASE_URL_USER = os.environ.get("TEST_USER_URL", "http://192.168.100.128:80")
BASE_URL_ADMIN = os.environ.get("TEST_ADMIN_URL", "http://192.168.100.128:81")

TEST_USER = os.environ.get("TEST_USER_USERNAME", "zhangsan")
TEST_USER_PASS = os.environ.get("TEST_USER_PASSWORD", "123456")
TEST_ADMIN = os.environ.get("TEST_ADMIN_USERNAME", "sunxiaoming")
TEST_ADMIN_PASS = os.environ.get("TEST_ADMIN_PASSWORD", "123456")


# ============================================================
# 页面基类
# ============================================================
class Page:
    """封装通用的等待、查找、输入、截图操作"""

    def __init__(self, driver, base_url=""):
        self.driver = driver
        self.base_url = base_url
        self.wait = WebDriverWait(driver, 10)

    def open(self, path=""):
        url = f"{self.base_url}{path}" if self.base_url else path
        self.driver.get(url)

    def find(self, by, value, timeout=10):
        wait = WebDriverWait(self.driver, timeout)
        return wait.until(EC.presence_of_element_located((by, value)))

    def find_visible(self, by, value, timeout=10):
        wait = WebDriverWait(self.driver, timeout)
        return wait.until(EC.visibility_of_element_located((by, value)))

    def find_all(self, by, value, timeout=10):
        self.find(by, value, timeout)
        return self.driver.find_elements(by, value)

    def click(self, by, value):
        el = self.find_visible(by, value)
        self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", el)
        time.sleep(0.2)
        el.click()

    def input_text(self, by, value, text):
        el = self.find(by, value)
        self.driver.execute_script("arguments[0].scrollIntoView({block:'center'});", el)
        time.sleep(0.2)
        el.click()
        el.clear()
        el.send_keys(text)

    def get_text(self, by, value):
        return self.find_visible(by, value).text

    def element_exists(self, by, value, timeout=3):
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, value)))
            return True
        except:
            return False

    def screenshot(self, name):
        os.makedirs("test/selenium/screenshots", exist_ok=True)
        self.driver.save_screenshot(f"test/selenium/screenshots/{name}.png")


# ============================================================
# 用户端 LoginPage
# ============================================================
class UserLoginPage(Page):
    """
    用户端登录页 /login
    有登录/注册两个tab，默认选中登录tab
    """

    def ensure_login_tab(self):
        """确保在登录 tab（默认就是 login）"""
        time.sleep(1)
        # 如果没看到登录表单，点击登录 tab
        try:
            tabs = self.driver.find_elements(By.CSS_SELECTOR, ".el-tabs__item")
            for t in tabs:
                if t.text.strip() == "登录":
                    t.click()
                    time.sleep(0.5)
                    break
        except:
            pass

    def login(self, username=None, password=None):
        username = username or TEST_USER
        password = password or TEST_USER_PASS
        self.open("/login")
        time.sleep(2)
        self.ensure_login_tab()

        # 登录表单中使用 placeholder 定位
        self.input_text(By.CSS_SELECTOR, 'input[placeholder="用户名"]', username)
        self.input_text(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]', password)
        # 点击"登录"按钮
        self.click(By.XPATH, '//button[contains(@class,"el-button--primary")]//span[text()="登录"]/..')
        time.sleep(2)

    def is_logged_in(self):
        """检查是否已登录 — URL 不在 /login 页"""
        time.sleep(1)
        return "/login" not in self.driver.current_url


# ============================================================
# 管理端 LoginPage
# ============================================================
class AdminLoginPage(Page):
    """管理端登录页 /login — 仅登录表单，无注册"""

    def login(self, username=None, password=None):
        username = username or TEST_ADMIN
        password = password or TEST_ADMIN_PASS
        self.open("/login")
        time.sleep(2)

        self.input_text(By.CSS_SELECTOR, 'input[placeholder="用户名"]', username)
        self.input_text(By.CSS_SELECTOR, 'input[placeholder="密码"][type="password"]', password)
        # 管理端使用 class="submit-btn"
        self.click(By.CSS_SELECTOR, "button.submit-btn")
        time.sleep(2)

    def is_logged_in(self):
        time.sleep(1)
        return "/login" not in self.driver.current_url


# ============================================================
# 断言辅助
# ============================================================
def assert_success(message: str):
    print(f"  ✅ {message}")


def assert_equals(expected, actual, message: str):
    assert expected == actual, f"{message}: expect={expected}, actual={actual}"
    print(f"  ✅ {message}")


# ============================================================
# Pytest Fixtures
# ============================================================
@pytest.fixture(scope="session")
def driver():
    """创建 Edge 浏览器实例（headless）"""
    os.makedirs("test/selenium/screenshots", exist_ok=True)

    options = Options()
    options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--disable-gpu")

    service = Service(EdgeChromiumDriverManager().install())
    driver = webdriver.Edge(service=service, options=options)
    driver.implicitly_wait(5)
    yield driver
    driver.quit()


@pytest.fixture(scope="session")
def driver_visible():
    """创建有界面的 Edge 浏览器实例（调试用）"""
    os.makedirs("test/selenium/screenshots", exist_ok=True)

    options = Options()
    options.add_argument("--window-size=1920,1080")

    service = Service(EdgeChromiumDriverManager().install())
    driver = webdriver.Edge(service=service, options=options)
    driver.implicitly_wait(5)
    yield driver
    driver.quit()
