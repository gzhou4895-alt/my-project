#!/bin/bash
# DIY Part 1 - 添加自定义软件源

# 添加 OpenClash 源
echo 'src-git openclash https://github.com/vernesong/OpenClash.git' >> feeds.conf.default

# 更新和安装 feeds
./scripts/feeds update -a
./scripts/feeds install -a
