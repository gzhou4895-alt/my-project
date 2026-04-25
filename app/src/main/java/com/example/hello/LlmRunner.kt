package com.example.hello

class LlmRunner {

    fun run(input: String, callback: (String) -> Unit) {

        Thread {
            callback("模型开始加载...\n")

            // ⚠️ 这里先模拟 LiteRT（后面换真实模型）
            var output = ""

            for (c in input) {
                Thread.sleep(80) // 模拟推理延迟
                output += c
                callback(output + "\n")
            }

            callback("推理完成\n")
        }.start()
    }
}
