# Redmi Note 13 Pro KernelSU 安装指南

> 适用于 Redmi Note 13 Pro (2312DRA50C/E) 骁龙 685 版本

## 特点

- ✅ 无需解锁 Bootloader
- ✅ 内核级 Root，难以检测
- ✅ 支持 Zygisk（Shamiko 等模块）

## 前置条件

- Redmi Note 13 Pro (骁龙 685 版)
- 电脑（Windows/macOS/Linux）
- USB 数据线
- 电量 > 50%
- **备份数据！**

## 所需文件

| 文件 | 用途 | 下载 |
|------|------|------|
| KernelSU.apk | Root 管理器 | [GitHub Releases](https://github.com/tiann/KernelSU/releases) |
| boot-gki.img | 内核镜像 | [KernelSU CI](https://github.com/tiann/KernelSU/actions) 或 [酷安](https://www.coolapk.com/) |
| platform-tools | fastboot/adb | [Google](https://developer.android.com/tools/releases/platform-tools) |

## 快速安装

### 1. 准备环境

```bash
# 下载 Android Platform Tools
# https://developer.android.com/tools/releases/platform-tools

# 解压后添加环境变量，或直接使用
```

### 2. 提取原厂 boot.img（推荐）

```bash
# 方法：从官方 ROM 解压
# 或手机直接提取：
adb shell
su  # 需要临时 root
dd if=/dev/block/bootdevice/by-name/boot of=/sdcard/boot.img
adb pull /sdcard/boot.img
```

### 3. 刷入 KernelSU

```bash
# 进入 fastboot
adb reboot bootloader

# 刷入 KernelSU 内核
fastboot flash boot boot-gki.img

# 重启
fastboot reboot
```

### 4. 验证 Root

1. 安装 KernelSU.apk
2. 打开应用，应显示"工作中"
3. 测试：`su` 命令

## 一键脚本

使用 `flash-kernelsu.sh`：

```bash
chmod +x flash-kernelsu.sh
./flash-kernelsu.sh boot-gki.img
```

## 常见问题

### 刷入后不开机？

```bash
# 刷回原厂内核
fastboot flash boot stock_boot.img
fastboot reboot
```

### 系统更新后失效？

每次 OTA 后需重新刷入 KernelSU 内核。

### 与 Magisk 冲突？

KernelSU 和 Magisk 不能共存，需先卸载 Magisk。

## 资源链接

- [KernelSU 官方文档](https://kernelsu.org/)
- [XDA Redmi Note 13 Pro 板块](https://forum.xda-developers.com/f/xiaomi-redmi-note-13-pro.12793/)
- [酷安 KernelSU 话题](https://www.coolapk.com/)

## 免责声明

- 刷机有风险，操作需谨慎
- 可能导致数据丢失或设备损坏
- 自行承担风险

## License

MIT
