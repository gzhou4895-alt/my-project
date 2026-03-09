#!/bin/bash
#
# Copyright (c) 2019-2024 P3TERX <https://p3terx.com>
#
# This is free software, licensed under the MIT License.
# See /LICENSE for more information.
#
# https://github.com/P3TERX/Actions-OpenWrt
# File name: diy-part1.sh
# Description: OpenWrt DIY script part 1 (Before Update feeds)
#

# Uncomment a feed source
#sed -i 's/^#\(.*helloworld\)/\1/' feeds.conf.default

# Add a feed source
#echo 'src-git helloworld https://github.com/fw876/helloworld' >> feeds.conf.default
#echo 'src-git passwall https://github.com/xiaorouji/openwrt-passwall' >> feeds.conf.default

# 添加 OpenClash 源 (使用 dev 分支兼容 23.05)
echo 'src-git openclash https://github.com/vernesong/OpenClash.git;dev' >> feeds.conf.default

# 添加 GitHub 代理加速 (可选，国内用户建议启用)
# sed -i 's|https://github.com|https://ghproxy.com/https://github.com|g' feeds.conf.default
