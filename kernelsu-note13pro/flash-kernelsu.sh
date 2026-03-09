#!/bin/bash
# Redmi Note 13 Pro KernelSU 一键刷机脚本
# 无需解锁 Bootloader

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}====================================${NC}"
echo -e "${GREEN}  Redmi Note 13 Pro KernelSU 刷机工具${NC}"
echo -e "${BLUE}====================================${NC}"
echo ""

# 检查参数
if [ -z "$1" ]; then
    echo -e "${RED}用法: $0 <boot.img 路径>${NC}"
    echo ""
    echo "示例:"
    echo "  $0 boot-gki.img"
    echo "  $0 /path/to/boot.img"
    echo ""
    echo -e "${YELLOW}请先下载 KernelSU 内核镜像${NC}"
    exit 1
fi

BOOT_IMG="$1"

# 检查文件存在
if [ ! -f "$BOOT_IMG" ]; then
    echo -e "${RED}错误: 文件不存在: $BOOT_IMG${NC}"
    exit 1
fi

# 检查 adb/fastboot
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}错误: 未找到 $1${NC}"
        echo "请安装 Android Platform Tools:"
        echo "https://developer.android.com/tools/releases/platform-tools"
        exit 1
    fi
}

echo -e "${GREEN}[1/5] 检查环境...${NC}"
check_command adb
check_command fastboot
echo -e "${YELLOW}✓ adb 和 fastboot 已就绪${NC}"

# 检查设备连接
echo -e "${GREEN}[2/5] 检查设备连接...${NC}"
adb devices | grep -q "device$" || {
    echo -e "${RED}错误: 未检测到已连接的设备${NC}"
    echo "请确保："
    echo "  1. USB 调试已开启"
    echo "  2. 已授权电脑调试"
    echo "  3. 数据线连接正常"
    adb devices
    exit 1
}
echo -e "${YELLOW}✓ 设备已连接${NC}"

# 进入 fastboot
echo -e "${GREEN}[3/5] 进入 Fastboot 模式...${NC}"
echo -e "${YELLOW}正在重启到 Fastboot...${NC}"
adb reboot bootloader

# 等待设备进入 fastboot
echo -e "${YELLOW}等待设备进入 Fastboot (约 5-10 秒)...${NC}"
sleep 8

# 检查 fastboot 连接
fastboot devices | grep -q "fastboot" || {
    echo -e "${RED}错误: 设备未进入 Fastboot 模式${NC}"
    echo "请手动进入 Fastboot: 关机后按住 音量下 + 电源键"
    exit 1
}
echo -e "${YELLOW}✓ 已进入 Fastboot 模式${NC}"

# 刷入内核
echo -e "${GREEN}[4/5] 刷入 KernelSU 内核...${NC}"
echo -e "${YELLOW}正在刷入: $BOOT_IMG${NC}"
fastboot flash boot "$BOOT_IMG"
echo -e "${YELLOW}✓ 内核刷入完成${NC}"

# 重启
echo -e "${GREEN}[5/5] 重启设备...${NC}"
echo -e "${YELLOW}正在重启...${NC}"
fastboot reboot

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}      刷机完成！${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo -e "${YELLOW}下一步:${NC}"
echo "  1. 等待手机开机（首次可能较慢）"
echo "  2. 安装 KernelSU.apk"
echo "  3. 打开应用验证 Root 状态"
echo ""
echo -e "${YELLOW}如果无法开机:${NC}"
echo "  重新进入 Fastboot，刷回原厂 boot.img:"
echo "  fastboot flash boot stock_boot.img"
echo ""
echo -e "${BLUE}有问题访问: https://kernelsu.org${NC}"
