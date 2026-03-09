#!/bin/bash
# DIY Part 1 - 添加自定义软件源

# feeds.conf.default 已经包含 OpenClash 源，这里不需要重复添加
# 如果需要添加其他源，可以在这里添加

# 更新和安装 feeds
./scripts/feeds update -a
./scripts/feeds install -a
