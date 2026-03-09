# Redmi Note 13 Pro EDL (9008) 硬刷指南

> 适用于未解锁 Bootloader 的 Redmi Note 13 Pro (骁龙 685)
> ⚠️ 需要拆机，有风险！

## 什么是 EDL 模式？

EDL（Emergency Download Mode）是高通芯片的紧急下载模式，可以：
- ✅ 绕过 Bootloader 锁
- ✅ 直接刷写分区（boot/system等）
- ✅ 救砖（设备变砖后恢复）

## 前置条件

### 硬件准备
| 工具 | 说明 |
|------|------|
| **拆机工具** | 吸盘、塑料撬棒、螺丝刀 |
| **短接工具** | 镊子、铜线、或 9008 工程线 |
| **数据线** | 质量好的 USB 数据线 |
| **电脑** | Windows 系统（QPST 工具）|

### 软件准备
| 软件 | 下载 |
|------|------|
| **QPST** | https://qpsttool.com/ |
| **QFIL** | 随 QPST 安装 |
| **高通驱动** | https:// Qualcomm_Driver.exe |
| **9008 驱动** | QDLoader HS-USB Driver |

---

## 步骤一：拆机

### 1.1 拆后盖
```
1. 关机
2. 用吹风机加热后盖边缘（软化胶水）
3. 用吸盘吸住后盖，插入塑料撬棒
4. 沿边缘慢慢撬开（注意指纹排线！）
```

### 1.2 取下主板保护盖
```
1. 拧下所有螺丝
2. 断开电池排线（先断电！）
3. 取下金属保护盖
```

---

## 步骤二：找到 EDL 测试点

### Redmi Note 13 Pro 测试点位置

```
主板布局（大概位置）：

      [摄像头模组]
           |
    ┌──────┴──────┐
    │             │
    │   [SOC]     │  ← 骁龙 685 芯片
    │             │
    │  ●────●     │  ← EDL 测试点（两个触点）
    │  TP1  TP2   │
    │             │
    └─────────────┘
         |
    [SIM 卡槽]
```

### 具体位置描述
- **位置**：主板背面，SIM 卡槽附近
- **外观**：两个金色小圆点（Test Point）
- **标识**：可能标有 `EDL`、`9008`、`TP` 字样
- **距离**：两个点相距约 2-3mm

### 找不到测试点？
1. **查图纸**：搜索 "Redmi Note 13 Pro schematic" 或 "garnet schematic"
2. **看芯片**：测试点通常在骁龙芯片附近
3. **万用表**：一个点接地，一个点接芯片的 EDL 引脚
4. **问社区**：酷安/XDA 搜索 "Note 13 Pro 9008 测试点"

---

## 步骤三：进入 EDL 模式

### 方法一：镊子短接（最常用）

```bash
1. 电脑安装 QPST 和驱动
2. 手机完全关机
3. 用镊子/铜线 短接两个 EDL 测试点
4. 保持短接，插入 USB 数据线连接电脑
5. 电脑提示 "QDLoader 9008" 或听到连接声音
6. 松开短接工具
```

### 方法二：9008 工程线（推荐）

```bash
# 淘宝购买 "9008 工程线" 或 "小米工程线"（约 ¥10-30）

使用方法：
1. 工程线一端接手机
2. 按住工程线上的按钮
3. 插入电脑 USB
4. 电脑识别为 9008 模式
5. 松开按钮
```

### 方法三：Deep Flash 线

```bash
# 专门针对小米/红米的工程线
# 自动进入 EDL 模式，无需短接
```

---

## 步骤四：使用 QFIL 刷机

### 4.1 确认连接
```
设备管理器 → 端口(COM和LPT) → 应显示：
"Qualcomm HS-USB QDLoader 9008 (COMxx)"
```

### 4.2 QFIL 刷入步骤

```
1. 打开 QFIL (开始菜单 → QPST → QFIL)

2. 选择配置：
   Select Build Type: Flat Build
   Programmer Path: 选择对应的 prog_emmc_firehose_xxxx.mbn
   (需下载对应机型的 firehose 文件)

3. 选择分区：
   Load XML → 选择 rawprogram0.xml
   Load Patch → 选择 patch0.xml

4. 刷入 boot：
   或手动选择 Partition → boot → 选择 KernelSU 的 boot.img

5. 点击 Download 开始刷入

6. 等待完成（绿色 PASS）
```

---

## 常见问题

### Q: 电脑不识别 9008 端口？
```
解决：
1. 检查驱动是否安装正确
2. 更换 USB 口（建议 USB 2.0）
3. 更换数据线
4. 重新短接尝试
5. 检查测试点是否正确
```

### Q: QFIL 报错 " Sahara 失败"？
```
原因：firehose 文件不匹配
解决：
1. 下载对应机型的 firehose 文件
2. 骁龙 685 需 SM6225 的 firehose
3. 从官方固件包提取 firehose
```

### Q: 刷入后不开机？
```
救砖：
1. 重新进入 9008 模式
2. 刷入完整官方固件包
3. 或只刷回原厂 boot.img
```

### Q: 找不到 firehose 文件？
```
获取途径：
1. 官方 ROM 包（.tgz 解压后找 prog_emmc_firehose_*.mbn）
2. XDA 论坛搜索 "Redmi Note 13 Pro firehose"
3. 高通固件数据库：https://...（需科学上网）
```

---

## 🔥 一键 EDL 刷机脚本

使用 `edl-flash.sh`：

```bash
# 自动检测 9008 端口并刷入
./edl-flash.sh boot-gki.img
```

---

## ⚠️ 风险提示

| 风险 | 后果 |
|------|------|
| **拆机损坏** | 排线断裂、元件损坏 |
| **短接错误** | 主板短路、烧毁芯片 |
| **刷错固件** | 设备变砖 |
| **失去保修** | 拆机后官方不保修 |

---

## 📚 资源链接

- **QPST 下载**: https://qpsttool.com/
- **9008 驱动**: https://...
- **XDA 板块**: https://forum.xda-developers.com/f/xiaomi-redmi-note-13-pro.12793/
- **酷安**: https://www.coolapk.com/search?keyword=Note%2013%20Pro%209008

---

## 免责声明

拆机刷机有风险，操作需谨慎。自行承担后果。
