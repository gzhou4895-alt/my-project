package com.example.hello

import android.util.Log

/**
 * 修复后的 LlmRunner
 * 不再持有 getResponse 同步方法，而是作为异步发送的中转站
 */
object LlmRunner {
    private const val TAG = "LlmRunner"

    /**
     * 执行推理任务
     * 结果将通过 GemmaEngine 中设置的 setOnResultListener 异步返回给 UI
     */
    fun run(prompt: String) {
        if (GemmaEngine.isReady()) {
            Log.d(TAG, "正在分发异步任务: $prompt")
            GemmaEngine.sendPrompt(prompt)
        } else {
            Log.e(TAG, "引擎尚未就绪，无法执行任务")
        }
    }
    
    /**
     * 如果你后续想在 Runner 层做一些 Prompt 预处理（比如加点前缀），可以在这里写
     */
    fun runWithTemplate(userPrompt: String) {
        val templatePrompt = "请用简洁的语言回答：$userPrompt"
        run(templatePrompt)
    }
}
