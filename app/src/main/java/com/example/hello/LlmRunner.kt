package com.example.hello

class LlmRunner {

    /**
     * 模拟 LiteRT 推理（流式输出版本）
     * 你后面只需要把这里替换成真正 LiteRT 推理即可
     */
    fun run(input: String, callback: (String) -> Unit) {

        Thread {

            try {
                callback("🤖 模型开始加载...\n")

                // 模拟加载时间
                Thread.sleep(500)

                callback("🤖 模型加载完成\n")
                callback("🤖 开始推理...\n")

                // 模拟“流式输出”（ChatGPT效果关键）
                var output = ""

                for (c in input) {
                    Thread.sleep(80) // 模拟 token 延迟
                    output += c
                    callback("token: $output")
                }

                Thread.sleep(300)

                callback("\n🤖 推理完成\n")

            } catch (e: Exception) {
                callback("❌ 推理失败: ${e.message}")
            }
        }.start()
    }
}
