package com.example.hello

import android.os.Handler
import android.os.Looper

class LlmRunner {

    /**
     * 真正的推理执行方法
     * 已经删除了模拟的 "token:" 等冗余日志信息
     */
    fun run(input: String, callback: (String) -> Unit) {

        // 获取主线程 Handler，确保 UI 更新不会崩溃
        val mainHandler = Handler(Looper.getMainLooper())

        Thread {
            try {
                // 1. 调用刚才我们在 GemmaEngine 封装好的真实推理方法
                val response = GemmaEngine.getResponse(input)

                // 2. 将结果传回给回调函数
                mainHandler.post {
                    callback(response)
                }

            } catch (e: Exception) {
                mainHandler.post {
                    callback("❌ 推理失败: ${e.message}")
                }
            }
        }.start()
    }
}
