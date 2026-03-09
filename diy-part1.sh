#!/bin/bash
# DIY Part 1 - 添加自定义软件源（在 Load Custom Feeds 步骤后执行）

# 确保在 openwrt 目录下
cd openwrt 2>/dev/null || cd $GITHUB_WORKSPACE/openwrt 2>/dev/null || true

echo "当前目录: $(pwd)"
echo "DIY Part 1 完成"
# 注意：feeds update 和 install 在工作流的独立步骤中执行，这里不需要重复
