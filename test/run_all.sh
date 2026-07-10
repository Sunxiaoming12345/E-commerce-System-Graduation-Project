#!/bin/bash
# ============================================================
# 一键运行全部测试
# 用法:
#   bash test/run_all.sh              # 全部测试
#   bash test/run_all.sh shell        # 仅 Shell  测试
#   bash test/run_all.sh selenium     # 仅 Selenium 测试
# ============================================================

set -e

cd "$(dirname "$0")/.."  # 回到项目根目录

GREEN='\033[32m'
RED='\033[31m'
BLUE='\033[34m'
NC='\033[0m'

EXIT_CODE=0

run_shell() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  Shell 并发测试套件${NC}"
    echo -e "${BLUE}============================================================${NC}"
    bash test/shell/concurrent_test.sh || EXIT_CODE=1
    echo ""

    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  Shell API 冒烟测试${NC}"
    echo -e "${BLUE}============================================================${NC}"
    bash test/shell/api_smoke_test.sh || EXIT_CODE=1
    echo ""

    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  Shell 数据一致性测试${NC}"
    echo -e "${BLUE}============================================================${NC}"
    bash test/shell/data_consistency.sh || EXIT_CODE=1
    echo ""
}

run_selenium() {
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  Selenium 用户端 E2E 测试${NC}"
    echo -e "${BLUE}============================================================${NC}"
    python test/selenium/test_user_flow.py || EXIT_CODE=1
    echo ""

    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  Selenium 管理端 E2E 测试${NC}"
    echo -e "${BLUE}============================================================${NC}"
    python test/selenium/test_admin_flow.py || EXIT_CODE=1
    echo ""
}

case "${1:-all}" in
    shell)
        run_shell
        ;;
    selenium)
        run_selenium
        ;;
    all|*)
        run_shell
        run_selenium
        ;;
esac

echo -e "${BLUE}============================================================${NC}"
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}  全部测试通过!${NC}"
else
    echo -e "${RED}  有测试失败，请查看上方日志${NC}"
fi
echo -e "${BLUE}============================================================${NC}"

exit $EXIT_CODE
