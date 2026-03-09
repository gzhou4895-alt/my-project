#!/bin/bash
# DIY Part 2 - 自定义配置

# 修改默认 IP
sed -i 's/192.168.1.1/192.168.8.1/g' package/base-files/files/bin/config_generate

# 修改主机名
sed -i 's/OpenWrt/GL-MT300N-V2/g' package/base-files/files/bin/config_generate

# 设置时区
sed -i "s/'UTC'/'CST-8'\n        set system.@system[-1].zonename='Asia\/Shanghai'/g" package/base-files/files/bin/config_generate

# 添加 OpenClash 初始化配置目录
mkdir -p files/etc/openclash

# 生成配置
defconfig
cat .config
