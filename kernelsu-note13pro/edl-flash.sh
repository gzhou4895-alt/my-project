#!/bin/bash
# Redmi Note 13 Pro EDL (9008) 一键刷机脚本
# 适用于未解锁 Bootloader 的设备

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════╗"
echo "║   Redmi Note 13 Pro EDL (9008) 刷机工具        ║"
echo "║   绕过 Bootloader 锁直接刷入 KernelSU          ║"
echo "╚════════════════════════════════════════════════╝"
echo -e "${NC}"
echo -e "${RED}警告: 需要拆机短接测试点！${NC}"
echo ""

# 检查参数
if [ -z "$1" ]; then
    echo -e "${RED}用法: $0 <boot.img 路径>${NC}"
    echo ""
    echo -e "${YELLOW}使用步骤:${NC}"
    echo "  1. 拆机找到主板 EDL 测试点（两个金色触点）"
    echo "  2. 用镊子短接两个测试点"
    echo "  3. 保持短接，插入 USB 连接电脑"
    echo "  4. 运行此脚本刷入内核"
    echo ""
    echo "示例:"
    echo "  $0 boot-gki.img"
    echo "  $0 ~/Downloads/kernelsu-boot.img"
    exit 1
fi

BOOT_IMG="$1"

# 检查文件
if [ ! -f "$BOOT_IMG" ]; then
    echo -e "${RED}错误: 文件不存在: $BOOT_IMG${NC}"
    exit 1
fi

echo -e "${CYAN}内核镜像: $BOOT_IMG${NC}"
echo ""

# 检查是否为 Windows（EDL 工具主要在 Windows）
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "win32" ]]; then
    echo -e "${GREEN}[✓] Windows 系统检测${NC}"
    IS_WINDOWS=1
else
    echo -e "${YELLOW}[!] 检测到非 Windows 系统${NC}"
    echo -e "${YELLOW}    EDL 工具主要在 Windows 下工作${NC}"
    echo -e "${YELLOW}    建议使用 Windows 运行此脚本${NC}"
    echo ""
    read -p "是否继续检查? (y/N): " confirm
    [[ "$confirm" =~ ^[Yy]$ ]] || exit 1
    IS_WINDOWS=0
fi

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${YELLOW}请按以下步骤操作:${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo -e "${CYAN}步骤 1: 准备工作${NC}"
echo "  • 拆机工具: 吸盘、塑料撬棒、螺丝刀"
echo "  • 短接工具: 镊子或铜线"
echo "  • 确保电脑已安装 QPST/QFIL"
echo ""
echo -e "${CYAN}步骤 2: 拆机${NC}"
echo "  1. 关机"
echo "  2. 吹风机加热后盖边缘"
echo "  3. 用吸盘+撬棒打开后盖"
echo "  4. 断开电池排线（重要！）"
echo "  5. 取下主板保护盖"
echo ""
echo -e "${CYAN}步骤 3: 找到 EDL 测试点${NC}"
echo "  位置: 主板背面，SIM 卡槽附近"
echo "  外观: 两个金色小圆点，相距 2-3mm"
echo "  可能在骁龙 685 芯片附近"
echo ""
echo -e "${CYAN}步骤 4: 进入 EDL 模式${NC}"
echo "  1. 电脑打开 QFIL 软件"
echo "  2. 用镊子短接两个 EDL 测试点"
echo "  3. 保持短接，插入 USB 数据线"
echo "  4. 等待电脑识别 9008 端口"
echo "  5. 松开镊子"
echo ""

read -p "完成上述步骤后按回车继续..."

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${YELLOW}检查 EDL 连接...${NC}"
echo -e "${GREEN}====================================${NC}"

# 检查 9008 端口
if [ "$IS_WINDOWS" -eq 1 ]; then
    # Windows: 使用设备管理器检查
    echo -e "${YELLOW}请手动检查设备管理器:${NC}"
    echo "  端口(COM和LPT) → Qualcomm HS-USB QDLoader 9008"
    echo ""
    read -p "是否识别到 9008 端口? (y/N): " has_9008
    if [[ ! "$has_9008" =~ ^[Yy]$ ]]; then
        echo -e "${RED}错误: 未进入 EDL 模式${NC}"
        echo -e "${YELLOW}请重新短接测试点${NC}"
        exit 1
    fi
else
    # Linux/Mac: 使用 lsusb
    echo -e "${YELLOW}检查 USB 设备...${NC}"
    lsusb | grep -i "qualcomm" || {
        echo -e "${YELLOW}未检测到高通设备，请检查连接${NC}"
    }
fi

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${YELLOW}QFIL 刷机步骤:${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo -e "${CYAN}1. 打开 QFIL${NC}"
echo "   开始菜单 → QPST → QFIL"
echo ""
echo -e "${CYAN}2. 配置刷机${NC}"
echo "   Select Build Type: Flat Build"
echo "   Programmer: 选择 firehose 文件 (*.mbn)"
echo ""
echo -e "${CYAN}3. 选择分区${NC}"
echo "   选择 Patch 模式或手动选择 boot 分区"
echo ""
echo -e "${CYAN}4. 选择内核镜像${NC}"
echo "   文件路径: $BOOT_IMG"
echo ""
echo -e "${CYAN}5. 开始刷机${NC}"
echo "   点击 Download，等待绿色 PASS"
echo ""
echo -e "${CYAN}6. 完成${NC}"
echo "   长按电源键重启手机"
echo ""

read -p "按回车打开 QFIL 刷机..."

# 尝试打开 QFIL（Windows）
if [ "$IS_WINDOWS" -eq 1 ]; then
    QFIL_PATH="C:\\Program Files (x86)\\Qualcomm\\QPST\\bin\\QFIL.exe"
    if [ -f "$QFIL_PATH" ]; then
        start "$QFIL_PATH"
        echo -e "${GREEN}[✓] 已启动 QFIL${NC}"
    else
        echo -e "${YELLOW}[!] 未找到 QFIL，请手动打开${NC}"
        echo "路径: C:\\Program Files (x86)\\Qualcomm\\QPST\\bin\\QFIL.exe"
    fi
fi

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}      刷机指引完成！${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo -e "${YELLOW}刷机后:${NC}"
echo "  1. 安装 KernelSU.apk"
echo "  2. 检查 Root 状态"
echo ""
echo -e "${YELLOW}救砖:${NC}"
echo "  如果无法开机，重新进入 EDL 刷入官方固件"
echo ""
echo -e "${BLUE}详细图文教程: EDL-GUIDE.md${NC}"
echo ""
