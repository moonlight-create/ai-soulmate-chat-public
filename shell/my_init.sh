#!/bin/bash
set -euo pipefail

# ========= 全局配置项 =========
REDIS_PASSWORD="Wj@1245241982"
CHROMA_PORT="8000"
CHROMA_CONTAINER_NAME="chroma"
NGINX_PORT="6666"
# 重试配置
MAX_RETRY=20
SLEEP_SEC=2

# ========= 0. 删除无效的 Docker 源，避免干扰 =========
echo -e "\n=== 删除无效的 Docker 源，避免干扰 ==="
sudo rm -f /etc/yum.repos.d/docker.repo
sudo dnf clean all
sudo dnf makecache

# ========= 1. 更新系统 + 处理 EPEL 冲突（保留阿里云EPEL，卸载官方epel-release） =========
echo -e "\n=== 更新系统并处理 EPEL 源冲突 ==="
if rpm -q epel-release &> /dev/null; then
    echo "检测到冲突包 epel-release，开始卸载..."
    sudo dnf remove -y epel-release
fi

sudo dnf update -y

# ========= 2. 检查并安装 JDK 17 =========
echo -e "\n=== 检查 OpenJDK 17 ==="
if java -version 2>&1 | grep -q 'openjdk version "17'; then
    echo "✅ JDK 17 已存在，跳过安装"
else
    echo "🔧 未检测到 JDK 17，开始安装..."
    sudo dnf install -y java-17-openjdk-devel
    echo "✅ JDK 17 安装成功"
fi

# ========= 3. 检查并安装、配置 Redis =========
echo -e "\n=== 检查 Redis ==="
if command -v redis-server &> /dev/null; then
    echo "✅ Redis 已存在，跳过安装"
else
    echo "🔧 未检测到 Redis，开始安装..."
    sudo dnf install -y redis
fi

REDIS_CONF="/etc/redis.conf"
if [ ! -f "$REDIS_CONF" ]; then
    echo "❌ 未找到配置文件 /etc/redis.conf，请检查 Redis 安装"
    exit 1
fi
echo "✅ 使用配置文件：$REDIS_CONF"

echo -e "\n=== 配置 Redis 服务 ==="
sudo cp "$REDIS_CONF" "${REDIS_CONF}.bak.$(date +%Y%m%d%H%M%S)"
sudo sed -i 's/^bind 127.0.0.1 -::1/bind 0.0.0.0/' "$REDIS_CONF"
sudo sed -i 's/^protected-mode yes/protected-mode no/' "$REDIS_CONF"
sudo sed -i "/^#*requirepass/c requirepass $REDIS_PASSWORD" "$REDIS_CONF"
sudo sed -i 's/^appendonly no/appendonly yes/' "$REDIS_CONF"
sync

echo -e "\n=== 重启 Redis 服务 ==="
sudo systemctl daemon-reload
sudo systemctl restart redis
sudo systemctl enable redis
echo "等待 Redis 服务启动..."
sleep 5

echo -e "\n=== 验证 Redis 连接 ==="
redis-cli CONFIG SET requirepass "$REDIS_PASSWORD"
redis-cli -a "$REDIS_PASSWORD" CONFIG REWRITE
redis-cli -a "$REDIS_PASSWORD" ping
if [ $? -eq 0 ]; then
    echo "✅ Redis 配置生效，连接正常，密码：$REDIS_PASSWORD"
else
    echo "❌ Redis 配置失败，请手动检查"
    exit 1
fi

# ========= 4. 检查并安装 Nginx + 修改监听端口为 1234 =========
echo -e "\n=== 检查 Nginx ==="
if command -v nginx &> /dev/null; then
    echo "✅ Nginx 已存在，跳过安装"
else
    echo "🔧 未检测到 Nginx，开始安装..."
    sudo dnf install -y nginx
fi

NGINX_CONF="/etc/nginx/nginx.conf"
sudo sed -i "s/listen       80;/listen       ${NGINX_PORT};/" "$NGINX_CONF"
sudo systemctl start nginx
sudo systemctl enable nginx
echo "✅ Nginx 已启动，监听端口：${NGINX_PORT}，已设置开机自启"

# ========= 5. 启用 podman-docker 兼容模式（无需安装 Docker） =========
echo -e "\n=== 启用 podman-docker 兼容模式 ==="
sudo dnf install -y podman-docker
sudo systemctl enable podman
sudo systemctl start podman

# 配置镜像加速器
sudo mkdir -p /etc/containers
sudo tee /etc/containers/registries.conf <<-'EOF'
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "docker.io"
[[registry.mirror]]
location = "392emwql.mirror.aliyuncs.com"
EOF

echo "🔧 等待 podman 服务就绪..."
retry=0
while ! sudo docker info &> /dev/null; do
    retry=$((retry+1))
    if [ $retry -ge $MAX_RETRY ]; then
        echo "❌ podman 启动超时，退出"
        exit 1
    fi
    echo "podman 未就绪，第 $retry 次重试，等待 ${SLEEP_SEC}s..."
    sleep ${SLEEP_SEC}
done
echo "✅ podman 服务已就绪，兼容 docker 命令"

# 部署 Chroma 向量数据库容器（改为离线导入提示）
echo -e "\n=== 检查 Chroma 容器 ==="
if sudo docker ps -a --filter "name=^/${CHROMA_CONTAINER_NAME}$" | grep -q "${CHROMA_CONTAINER_NAME}"; then
    echo "✅ Chroma 容器已存在"
    if ! sudo docker ps --filter "name=^/${CHROMA_CONTAINER_NAME}$" | grep -q "${CHROMA_CONTAINER_NAME}"; then
        echo "🔧 启动已存在的 Chroma 容器..."
        sudo docker start ${CHROMA_CONTAINER_NAME}
    fi
else
    echo "⚠️  由于网络限制，Chroma 镜像无法在线拉取，请执行以下步骤："
    echo "1. 在本地电脑执行：docker pull chromadb/chroma:latest && docker save -o chroma.tar chromadb/chroma:latest"
    echo "2. 将 chroma.tar 上传到服务器 /root 目录"
    echo "3. 执行导入命令：docker load -i /root/chroma.tar"
    echo "4. 再执行此脚本，容器会自动启动"
    echo "🔧 脚本将跳过拉取，直接等待你手动导入镜像..."
fi

# -------- Chroma 端口监听 循环重试检测（可选） --------
if sudo docker ps -a --filter "name=^/${CHROMA_CONTAINER_NAME}$" | grep -q "${CHROMA_CONTAINER_NAME}"; then
    echo -e "\n=== 等待 Chroma 端口监听就绪 ==="
    retry=0
    while ! sudo ss -tulpn | grep -q ":${CHROMA_PORT}"; do
        retry=$((retry+1))
        if [ $retry -ge $MAX_RETRY ]; then
            echo "❌ Chroma 端口监听超时，服务异常"
            exit 1
        fi
        echo "Chroma 未监听 ${CHROMA_PORT}，第 $retry 次重试，等待 ${SLEEP_SEC}s..."
        sleep ${SLEEP_SEC}
    done
    echo "✅ Chroma 正常监听 ${CHROMA_PORT} 端口"
fi

# ========= 最终汇总输出 =========
echo -e "\n======================================"
echo "🎉 全部环境安装&配置完成！"
echo "JDK 版本：$(java -version 2>&1 | head -n 1)"
echo "Redis 端口：6379"
echo "Nginx 端口：${NGINX_PORT}"
echo "Chroma 向量库 端口：${CHROMA_PORT}（需手动导入镜像后启动）"
echo "======================================"
echo "🔔 安全组务必放行端口：${NGINX_PORT}、6379、${CHROMA_PORT}"
echo "🔔 Spring AI 配置示例："
echo "spring.ai.vectorstore.chroma.client.host=服务器公网IP"
echo "spring.ai.vectorstore.chroma.client.port=${CHROMA_PORT}"
echo "======================================"
