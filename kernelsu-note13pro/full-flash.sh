#!/bin/bash
# ============================================
# Redmi Note 13 Pro KernelSU 完整刷机脚本
# 自动下载 + 一键刷入 + 备份恢复
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置
DEVICE_NAME="Redmi Note 13 Pro"
KERNELSU_VERSION="v1.0.2"
BACKUP_DIR="$HOME/kernelsu-backup"
TEMP_DIR="/tmp/kernelsu-flash"

# 下载链接
KERNELSU_APK_URL="https://github.com/tiann/KernelSU/releases/download/${KERNELSU_VERSION}/KernelSU_${KERNELSU_VERSION}-release.apk"
PLATFORM_TOOLS_URL="https://dl.google.com/android/repository/platform-tools-latest-linux.zip"

# 清除临时文件
cleanup() {
    rm -rf "$TEMP_DIR" 2>/dev/null || true
}
trap cleanup EXIT

# 打印 banner
print_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════╗"
    echo "║   Redmi Note 13 Pro KernelSU 刷机工具    ║"
    echo "║        无需解锁 Bootloader               ║"
    echo "╚══════════════════════════════════════════╝"
    echo -e "${NC}"
    echo -e "${CYAN}设备: ${DEVICE_NAME}${NC}"
    echo -e "${CYAN}KernelSU: ${KERNELSU_VERSION}${NC}"
    echo ""
}

# 检查命令
check_command() {
    if ! command -v "$1" &> /dev/null; then
        return 1
    fi
    return 0
}

# 安装 adb/fastboot
install_platform_tools() {
    echo -e "${YELLOW}[*] 未检测到 adb/fastboot，正在安装...${NC}"
    
    mkdir -p "$TEMP_DIR"
    cd "$TEMP_DIR"
    
    echo -e "${YELLOW}[*] 下载 Android Platform Tools...${NC}"
    wget -q --show-progress "$PLATFORM_TOOLS_URL" -O platform-tools.zip
    
    echo -e "${YELLOW}[*] 解压...${NC}"
    unzip -q platform-tools.zip
    
    echo -e "${YELLOW}[*] 安装到 /usr/local/bin...${NC}"
    sudo cp platform-tools/adb /usr/local/bin/ 2>/dev/null || cp platform-tools/adb "$HOME/.local/bin/" 2>/dev/null || {
        echo -e "${RED}[!] 无法安装到系统目录，将使用临时路径${NC}"
        export PATH="$TEMP_DIR/platform-tools:$PATH"
    }
    sudo cp platform-tools/fastboot /usr/local/bin/ 2>/dev/null || cp platform-tools/fastboot "$HOME/.local/bin/" 2>/dev/null || true
    
    cd - > /dev/null
    echo -e "${GREEN}[✓] Platform Tools 安装完成${NC}"
}

# 检查环境
check_environment() {
    echo -e "${GREEN}[1/6] 检查环境...${NC}"
    
    # 检查 adb/fastboot
    if ! check_command adb || ! check_command fastboot; then
        install_platform_tools
    fi
    
    # 验证
    ADB_VERSION=$(adb version 2>/dev/null | head -1 || echo "unknown")
    echo -e "${CYAN}    $ADB_VERSION${NC}"
    
    # 检查依赖
    if ! check_command wget; then
        echo -e "${YELLOW}[*] 安装 wget...${NC}"
        apt-get update && apt-get install -y wget unzip curl
    fi
    
    # 创建备份目录
    mkdir -p "$BACKUP_DIR"
    
    echo -e "${GREEN}[✓] 环境检查完成${NC}"
}

# 检查设备连接
check_device() {
    echo -e "${GREEN}[2/6] 检查设备连接...${NC}"
    
    # 检查是否有设备
    DEVICE_COUNT=$(adb devices | grep -c "device$" || echo "0")
    
    if [ "$DEVICE_COUNT" -eq "0" ]; then
        echo -e "${RED}[!] 未检测到已连接的设备${NC}"
        echo -e "${YELLOW}请检查:${NC}"
        echo "  1. USB 调试已开启 (设置 → 开发者选项 → USB调试)"
        echo "  2. 已授权此电脑 (手机上点击"允许")"
        echo "  3. 数据线连接正常"
        echo ""
        echo -e "${CYAN}当前设备列表:${NC}"
        adb devices
        exit 1
    fi
    
    # 获取设备信息
    DEVICE_MODEL=$(adb shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    DEVICE_CODENAME=$(adb shell getprop ro.product.device 2>/dev/null | tr -d '\r')
    ANDROID_VERSION=$(adb shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')
    
    echo -e "${CYAN}    设备型号: ${DEVICE_MODEL}${NC}"
    echo -e "${CYAN}    代号: ${DEVICE_CODENAME}${NC}"
    echo -e "${CYAN}    Android: ${ANDROID_VERSION}${NC}"
    
    # 确认机型
    if [[ "$DEVICE_MODEL" != *"Note 13 Pro"* ]] && [[ "$DEVICE_CODENAME" != *"garnet"* ]]; then
        echo -e "${YELLOW}[!] 警告: 检测到非 Note 13 Pro 设备${NC}"
        echo -e "${YELLOW}    此脚本专为 Redmi Note 13 Pro 设计${NC}"
        read -p "是否继续? (y/N): " confirm
        if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    echo -e "${GREEN}[✓] 设备已连接${NC}"
}

# 下载 KernelSU
download_kernelsu() {
    echo -e "${GREEN}[3/6] 下载 KernelSU...${NC}"
    
    mkdir -p "$TEMP_DIR"
    cd "$TEMP_DIR"
    
    # 下载 APK
    if [ ! -f "KernelSU.apk" ]; then
        echo -e "${YELLOW}[*] 下载 KernelSU 管理器...${NC}"
        wget -q --show-progress "$KERNELSU_APK_URL" -O KernelSU.apk || {
            echo -e "${RED}[!] 下载失败，请手动下载:${NC}"
            echo "$KERNELSU_APK_URL"
            exit 1
        }
    fi
    
    # 推送到手机
    echo -e "${YELLOW}[*] 安装 KernelSU 管理器到手机...${NC}"
    adb install -r KernelSU.apk 2>/dev/null || {
        echo -e "${YELLOW}[!] 安装失败，请手动安装${NC}"
        cp KernelSU.apk "$HOME/Downloads/" 2>/dev/null || true
        echo -e "${CYAN}    APK 已保存到: $HOME/Downloads/KernelSU.apk${NC}"
    }
    
    echo -e "${GREEN}[✓] KernelSU 准备完成${NC}"
}

# 备份原厂 boot
backup_boot() {
    echo -e "${GREEN}[4/6] 备份原厂 boot 镜像...${NC}"
    
    BACKUP_FILE="$BACKUP_DIR/boot-$(date +%Y%m%d-%H%M%S).img"
    
    echo -e "${YELLOW}[*] 提取原厂 boot 镜像...${NC}"
    adb shell "dd if=/dev/block/bootdevice/by-name/boot of=/sdcard/stock_boot.img" 2>/dev/null || {
        echo -e "${YELLOW}[!] 无法提取 boot，可能需要 root${NC}"
        echo -e "${YELLOW}    请从官方 ROM 手动提取 boot.img${NC}"
        read -p "是否有本地 boot.img 文件? (y/N): " has_boot
        if [[ "$has_boot" =~ ^[Yy]$ ]]; then
            read -p "输入 boot.img 路径: " boot_path
            if [ -f "$boot_path" ]; then
                cp "$boot_path" "$BACKUP_FILE"
            else
                echo -e "${RED}[!] 文件不存在${NC}"
                exit 1
            fi
        fi
    }
    
    # 拉取备份
    if adb shell "ls /sdcard/stock_boot.img" &>/dev/null; then
        adb pull /sdcard/stock_boot.img "$BACKUP_FILE"
        adb shell "rm /sdcard/stock_boot.img"
        echo -e "${GREEN}[✓] 备份已保存: ${BACKUP_FILE}${NC}"
    fi
}

# 刷入内核
flash_kernel() {
    echo -e "${GREEN}[5/6] 刷入 KernelSU 内核...${NC}"
    
    # 查找内核镜像
    BOOT_IMG=""
    
    # 检查本地是否有内核镜像
    if [ -f "$1" ]; then
        BOOT_IMG="$1"
    elif [ -f "boot-gki.img" ]; then
        BOOT_IMG="boot-gki.img"
    elif [ -f "$TEMP_DIR/boot-gki.img" ]; then
        BOOT_IMG="$TEMP_DIR/boot-gki.img"
    fi
    
    # 如果没有，提示下载
    if [ -z "$BOOT_IMG" ]; then
        echo -e "${YELLOW}[!] 未找到 KernelSU 内核镜像${NC}"
        echo -e "${CYAN}请从以下地址下载:${NC}"
        echo "  1. KernelSU GitHub Actions: https://github.com/tiann/KernelSU/actions"
        echo "  2. 酷安: https://www.coolapk.com/search?keyword=KernelSU%20Note%2013%20Pro"
        echo "  3. XDA: https://forum.xda-developers.com/f/xiaomi-redmi-note-13-pro.12793/"
        echo ""
        read -p "输入内核镜像路径 (或拖入文件): " BOOT_IMG
        
        if [ ! -f "$BOOT_IMG" ]; then
            echo -e "${RED}[!] 文件不存在${NC}"
            exit 1
        fi
    fi
    
    echo -e "${CYAN}    使用内核: $BOOT_IMG${NC}"
    
    # 进入 fastboot
    echo -e "${YELLOW}[*] 重启到 Fastboot 模式...${NC}"
    adb reboot bootloader
    
    # 等待进入 fastboot
    echo -e "${YELLOW}[*] 等待设备进入 Fastboot...${NC}"
    sleep 10
    
    # 检查 fastboot 连接
    for i in {1..10}; do
        if fastboot devices | grep -q "fastboot"; then
            break
        fi
        echo -e "${YELLOW}    等待中... ($i/10)${NC}"
        sleep 2
    done
    
    if ! fastboot devices | grep -q "fastboot"; then
        echo -e "${RED}[!] 设备未进入 Fastboot 模式${NC}"
        echo -e "${YELLOW}请手动进入: 关机后按住 音量下 + 电源键${NC}"
        exit 1
    fi
    
    # 刷入内核
    echo -e "${YELLOW}[*] 刷入内核...${NC}"
    fastboot flash boot "$BOOT_IMG"
    
    echo -e "${GREEN}[✓] 内核刷入成功${NC}"
}

# 重启设备
reboot_device() {
    echo -e "${GREEN}[6/6] 重启设备...${NC}"
    
    echo -e "${YELLOW}[*] 正在重启...${NC}"
    fastboot reboot
    
    echo ""
    echo -e "${YELLOW}等待设备开机...${NC}"
    sleep 15
    
    # 等待 adb 连接
    for i in {1..30}; do
        if adb devices | grep -q "device$"; then
            break
        fi
        sleep 2
    done
    
    echo -e "${GREEN}[✓] 设备已重启${NC}"
}

# 完成提示
finish() {
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║         刷机完成！                       ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${CYAN}下一步操作:${NC}"
    echo "  1. 打开手机上的 KernelSU 应用"
    echo "  2. 检查状态是否显示"工作中""
    echo "  3. 安装 Termux 测试: su 命令"
    echo ""
    echo -e "${CYAN}如果无法开机:${NC}"
    echo "  1. 进入 Fastboot (音量下 + 电源键)"
    echo "  2. 运行: fastboot flash boot $BACKUP_DIR/boot-xxx.img"
    echo "  3. 运行: fastboot reboot"
    echo ""
    echo -e "${YELLOW}备份文件保存在: $BACKUP_DIR${NC}"
    echo ""
    echo -e "${BLUE}问题反馈: https://github.com/tiann/KernelSU${NC}"
}

# 主函数
main() {
    print_banner
    
    # 检查是否以 root 运行（Linux/Mac）
    if [[ "$OSTYPE" == "linux-gnu"* ]] && [ "$EUID" -ne 0 ]; then
        echo -e "${YELLOW}[!] 提示: 部分操作可能需要 sudo${NC}"
    fi
    
    # 确认
    echo -e "${RED}警告: 刷机有风险，请确保已备份重要数据！${NC}"
    read -p "是否继续? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo -e "${YELLOW}已取消${NC}"
        exit 0
    fi
    
    # 执行流程
    check_environment
    check_device
    download_kernelsu
    backup_boot
    flash_kernel "$1"
    reboot_device
    finish
}

# 运行
main "$@"
