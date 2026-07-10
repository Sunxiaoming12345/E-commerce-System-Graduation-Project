#!/bin/bash
# ============================================================
# CentOS 7 Docker 兼容版本安装脚本
#
# CentOS 7 限制：
#   - 内核 3.10 → Docker CE 最高 24.0.x（25+ 不再支持）
#   - glibc 2.17 → Docker Compose v2 插件不可用（需 glibc 2.32+）
#                 改用 docker-compose v1.29.2（Python 独立版）
#
# 用法（在 CentOS 7 VM 上以 root 执行）：
#   chmod +x install-docker-centos7.sh
#   ./install-docker-centos7.sh
# ============================================================

set -e

DOCKER_VERSION="24.0.9"
COMPOSE_VERSION="1.29.2"

echo ""
echo "========================================"
echo "  CentOS 7 Docker 兼容版本安装"
echo "  Docker CE: ${DOCKER_VERSION}"
echo "  Docker Compose: ${COMPOSE_VERSION} (v1 standalone)"
echo "========================================"
echo ""

# ---- 1. 卸载旧版本 ----
echo "[1/6] 卸载旧版本 Docker..."
yum remove -y docker \
              docker-client \
              docker-client-latest \
              docker-common \
              docker-latest \
              docker-latest-logrotate \
              docker-logrotate \
              docker-engine \
              docker-ce \
              docker-ce-cli \
              containerd.io \
              docker-compose-plugin \
              2>/dev/null || true

# 清理旧数据（可选，谨慎）
# rm -rf /var/lib/docker /var/lib/containerd

# ---- 2. 安装依赖 ----
echo "[2/6] 安装依赖..."
yum install -y yum-utils device-mapper-persistent-data lvm2

# ---- 3. 添加 Docker CE 仓库 ----
echo "[3/6] 添加 Docker CE 仓库..."
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# ---- 4. 安装指定版本 Docker CE ----
echo "[4/6] 安装 Docker CE ${DOCKER_VERSION}..."
yum install -y docker-ce-${DOCKER_VERSION} \
               docker-ce-cli-${DOCKER_VERSION} \
               containerd.io

# ---- 5. 安装 docker-compose v1（独立版，兼容 glibc 2.17） ----
echo "[5/6] 安装 docker-compose v${COMPOSE_VERSION}..."
curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" \
     -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# ---- 6. 配置 Docker ----
echo "[6/6] 配置 Docker..."

# 创建 daemon.json
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "storage-driver": "overlay2",
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "live-restore": true,
  "iptables": true,
  "ip-forward": true,
  "ip-masq": true
}
EOF

# systemd override：启动前清理残留 iptables 链 + 自动重启
mkdir -p /etc/systemd/system/docker.service.d
cat > /etc/systemd/system/docker.service.d/override.conf << 'EOF'
[Service]
ExecStartPre=-/sbin/iptables -t nat -F DOCKER
ExecStartPre=-/sbin/iptables -t filter -F DOCKER
ExecStartPre=-/sbin/iptables -t filter -F DOCKER-ISOLATION-STAGE-1
ExecStartPre=-/sbin/iptables -t filter -F DOCKER-USER
Restart=always
RestartSec=5
EOF

systemctl daemon-reload

# 确保内核转发开启
echo "net.ipv4.ip_forward = 1" > /etc/sysctl.d/99-docker.conf
sysctl -p /etc/sysctl.d/99-docker.conf

# 启动并设为开机自启
systemctl enable docker
systemctl start docker

# ---- 验证 ----
echo ""
echo "========================================"
echo "  安装完成！版本信息："
echo "========================================"
docker --version
docker-compose --version
echo ""
echo "  Docker 已启动并设为开机自启。"
echo "  systemd override 已配置（启动前清理 iptables + 自动重启）。"
echo "========================================"
