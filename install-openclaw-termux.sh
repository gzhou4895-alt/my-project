#!/bin/bash
# OpenClaw 2026.2.26 一键安装脚本 for Termux + Ubuntu
# Node.js 22 + 自动修复 npm 版本问题

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${GREEN}=== OpenClaw 2026.2.26 安装脚本 ===${NC}"
echo -e "${BLUE}Termux + Ubuntu | Node.js 22${NC}"
echo ""

# 检查是否在 Ubuntu proot 中
if [ ! -f /etc/os-release ]; then
    echo -e "${RED}错误：请在 Ubuntu proot 环境中运行${NC}"
    echo "先运行: proot-distro login ubuntu"
    exit 1
fi

echo -e "${GREEN}[1/8] 更新系统...${NC}"
apt update && apt upgrade -y

echo -e "${GREEN}[2/8] 安装基础依赖...${NC}"
apt install -y curl wget git nano build-essential python3 python3-pip ca-certificates gnupg

echo -e "${GREEN}[3/8] 安装 Node.js 22...${NC}"
# 彻底清理旧版本
apt remove -y nodejs npm 2>/dev/null || true
apt autoremove -y 2>/dev/null || true
rm -rf /usr/lib/node_modules/npm 2>/dev/null || true
rm -rf /usr/local/lib/node_modules 2>/dev/null || true

# 使用 NodeSource 安装 Node.js 22
curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
apt install -y nodejs

# 验证版本
echo -e "${YELLOW}✓ Node.js: $(node -v)${NC}"
echo -e "${YELLOW}✓ npm: $(npm -v)${NC}"

echo -e "${GREEN}[4/8] 修复 npm 并更新...${NC}"
# 强制更新 npm 到 v10
npm install -g npm@10.9.0

# 清理缓存
npm cache clean --force 2>/dev/null || true

# 修复全局权限
mkdir -p ~/.npm-global
npm config set prefix '~/.npm-global'
if ! grep -q ".npm-global/bin" ~/.bashrc; then
    echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.bashrc
fi
export PATH=~/.npm-global/bin:$PATH

echo -e "${GREEN}[5/8] 克隆 OpenClaw 2026.2.26...${NC}"
cd ~

# 备份旧版本
if [ -d "openclaw" ]; then
    BACKUP_NAME="openclaw.backup.$(date +%Y%m%d%H%M%S)"
    echo -e "${YELLOW}备份旧版本到: $BACKUP_NAME${NC}"
    mv openclaw "$BACKUP_NAME"
fi

# 克隆最新版本
git clone --depth 1 https://github.com/openclaw/openclaw.git
cd openclaw

# 显示版本
echo -e "${YELLOW}✓ OpenClaw 版本: $(git log -1 --format=%cd --date=short 2>/dev/null || echo 'latest')${NC}"

echo -e "${GREEN}[6/8] 安装依赖...${NC}"
# 使用 legacy-peer-deps 避免冲突
npm install --legacy-peer-deps

echo -e "${GREEN}[7/8] 构建项目...${NC}"
# 2026.2.26 可能需要构建
if [ -f "package.json" ]; then
    if grep -q '"build"' package.json; then
        npm run build
        echo -e "${YELLOW}✓ 构建完成${NC}"
    fi
fi

echo -e "${GREEN}[8/8] 安装 PM2...${NC}"
npm install -g pm2
pm2 --version

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}    OpenClaw 安装完成！${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo -e "${YELLOW}📋 下一步配置:${NC}"
echo "   cd ~/openclaw"
echo "   cp openclaw.example.json openclaw.json"
echo "   nano openclaw.json"
echo ""
echo -e "${YELLOW}🚀 启动方式:${NC}"
echo "   npm start                    # 开发模式"
echo "   npm run start:prod           # 生产模式"
echo "   pm2 start npm --name openclaw -- run start:prod  # 后台"
echo ""
echo -e "${YELLOW}📊 版本信息:${NC}"
echo "   Node.js: $(node -v)"
echo "   npm: $(npm -v)"
echo "   PM2: $(pm2 --version | head -1)"
echo ""
echo -e "${BLUE}有问题访问: https://docs.openclaw.ai${NC}"
