package com.example.hello

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File
import java.util.concurrent.Executors

/**
 * 借鉴 Google AI Edge Gallery 官方实现
 * 使用 LlmInferenceSession 管理对话，提升稳定性
 */
object GemmaEngine {
    private var llmInference: LlmInference? = null
    private var llmSession: LlmInferenceSession? = null
    private val executor = Executors.newSingleThreadExecutor()
    private const val TAG = "GemmaEngine"

    fun isReady(): Boolean = llmInference != null && llmSession != null

    fun initialize(context: Context, callback: (Boolean) -> Unit) {
        if (isReady()) {
            callback(true)
            return
        }

        executor.execute {
            try {
                // 1. 严格对齐路径
                val folder = context.getExternalFilesDir(null)
                val modelFile = File(folder, "gemma-4-E2B-it.litertlm")

                if (!modelFile.exists()) {
                    Log.e(TAG, "❌ 找不到模型文件: ${modelFile.absolutePath}")
                    callback(false)
                    return@execute
                }

                // 2. 借鉴官方配置：LlmInferenceOptions
                // 骁龙 7 Gen 1 建议保持 TopK 为 40，这是性能平衡点
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setResultListener { result, done ->
                        // 如果你后续想做流式输出（打字机效果），在这里处理
                    }
                    .build()

                // 3. 初始化推理引擎
                val inference = LlmInference.createFromOptions(context, options)
                llmInference = inference

                // 4. 重点：创建 Session（官方丝滑的秘诀）
                // Session 负责管理 KV 缓存，避免重复计算，极大减少 RET_CHECK 报错
                val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTemperature(0.7f)
                    .setTopK(40)
                    .build()
                
                llmSession = LlmInferenceSession.createFromOptions(inference, sessionOptions)

                // 5. 官方习惯：静默预热一次
                llmSession?.addQueryChunk(" ")
                llmSession?.generateResponse()

                Log.d(TAG, "✅ 官方级引擎与 Session 初始化完成")
                callback(true)

            } catch (e: Exception) {
                Log.e(TAG, "💥 初始化失败，请检查模型文件或显存: ${e.message}")
                close()
                callback(false)
            }
        }
    }

    /**
     * 借鉴官方：通过 Session 生成回复
     */
    fun getResponse(prompt: String): String {
        val session = llmSession ?: return "引擎尚未初始化"
        return try {
            // 1. 将用户输入加入会话
            session.addQueryChunk(prompt)
            // 2. 生成结果
            val response = session.generateResponse()
            if (response.isNullOrBlank()) "（模型没有给出回答）" else response
        } catch (e: Exception) {
            Log.e(TAG, "推理过程异常: ${e.message}")
            "推理出错: ${e.localizedMessage}"
        }
    }

    /**
     * 清理资源（模仿官方生命周期管理）
     */
    fun close() {
        try {
            llmSession?.close()
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭资源失败: ${e.message}")
        }
        llmSession = null
        llmInference = null
    }
}
