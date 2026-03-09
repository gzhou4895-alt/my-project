#!/bin/bash
# OpenClaw 一键安装脚本 for Termux + Ubuntu
# Node.js 22 + 自动修复 npm 版本问题

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== OpenClaw 安装脚本 (Termux + Ubuntu) ===${NC}"
echo -e "${YELLOW}Node.js 22 + 自动修复${NC}"
echo ""

# 检查是否在 Ubuntu proot 中
if [ ! -f /etc/os-release ]; then
    echo -e "${RED}错误：请在 Ubuntu proot 环境中运行此脚本${NC}"
    echo "运行: proot-distro login ubuntu"
    exit 1
fi

echo -e "${GREEN}[1/7] 更新系统...${NC}"
apt update && apt upgrade -y

echo -e "${GREEN}[2/7] 安装基础依赖...${NC}"
apt install -y curl wget git nano build-essential python3 ca-certificates gnupg

echo -e "${GREEN}[3/7] 安装 Node.js 22...${NC}"
# 清理旧版本
apt remove -y nodejs npm 2>/dev/null || true
rm -rf /usr/lib/node_modules/npm 2>/dev/null || true

# 使用 NodeSource 安装 Node.js 22
curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
apt install -y nodejs

# 验证版本
echo -e "${YELLOW}Node.js 版本: $(node -v)${NC}"
echo -e "${YELLOW}npm 版本: $(npm -v)${NC}"

echo -e "${GREEN}[4/7] 修复 npm 版本问题...${NC}"
# 强制更新 npm 到最新兼容版本
npm install -g npm@10

# 清理 npm 缓存
npm cache clean --force

# 修复权限
mkdir -p ~/.npm-global
npm config set prefix '~/.npm-global'
echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.bashrc
export PATH=~/.npm-global/bin:$PATH

echo -e "${GREEN}[5/7] 克隆 OpenClaw...${NC}"
cd ~
if [ -d "openclaw" ]; then
    echo -e "${YELLOW}检测到已存在的 openclaw 目录，备份中...${NC}"
    mv openclaw openclaw.backup.$(date +%Y%m%d%H%M%S)
fi

git clone https://github.com/openclaw/openclaw.git
cd openclaw

echo -e "${GREEN}[6/7] 安装 OpenClaw 依赖...${NC}"
# 使用 --legacy-peer-deps 避免依赖冲突
npm install --legacy-peer-deps

echo -e "${GREEN}[7/7] 安装 PM2...${NC}"
npm install -g pm2

echo ""
echo -e "${GREEN}=== 安装完成！ ===${NC}"
echo ""
echo -e "${YELLOW}下一步配置:${NC}"
echo "1. cd ~/openclaw"
echo "2. cp openclaw.example.json openclaw.json"
echo "3. nano openclaw.json  # 编辑你的配置"
echo ""
echo -e "${YELLOW}启动命令:${NC}"
echo "   npm start          # 前台运行"
echo "   pm2 start npm --name 'openclaw' -- start  # 后台运行"
echo ""
echo -e "${GREEN}Node.js: $(node -v) | npm: $(npm -v)${NC}"
