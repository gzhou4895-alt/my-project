package com.example.hello

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File
import java.util.concurrent.Executors

object GemmaEngine {
    private var llmInference: LlmInference? = null
    private var llmSession: LlmInferenceSession? = null // 借鉴：引入 Session 机制
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isInitializing = false

    fun isReady(): Boolean = llmInference != null && llmSession != null

    fun initialize(context: Context, callback: (Boolean) -> Unit) {
        if (isReady()) { callback(true); return }
        if (isInitializing) return
        isInitializing = true

        executor.execute {
            try {
                val modelFileName = "gemma-4-E2B-it.litertlm"
                val folder = context.getExternalFilesDir(null)
                val modelFile = File(folder, modelFileName)

                if (!modelFile.exists()) {
                    showError(context, "找不到模型文件，请检查路径。")
                    mainHandler.post { callback(false) }; return@execute
                }

                // --- 【核心借鉴 1：更稳健的配置】 ---
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(2048) // 提高长度
                    .apply {
                        // 尝试自动适配：如果你手机支持 GPU 则用 GPU，报错则建议改 CPU
                        // 注意：如果编译不通过，请检查 implementation 'com.google.mediapipe:tasks-genai' 的版本
                        // setPreferredBackend(LlmInference.Backend.GPU) 
                    }
                    .build()

                // 1. 创建推理引擎
                val inference = LlmInference.createFromOptions(context, options)
                llmInference = inference

                // --- 【核心借鉴 2：创建对话 Session】 ---
                // 这是官方保持流畅、不闪退的秘诀
                val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTemperature(0.7f)
                    .setTopK(40)
                    .build()
                
                llmSession = LlmInferenceSession.createFromOptions(inference, sessionOptions)

                Log.d("GemmaEngine", "✅ 引擎与 Session 均已就绪")
                mainHandler.post { callback(true) }

            } catch (e: Exception) {
                Log.e("GemmaEngine", "💥 失败详情: ${e.message}")
                showError(context, "启动失败: ${e.localizedMessage}\n建议换用 2B 模型或关闭其它后台。")
                close()
                mainHandler.post { callback(false) }
            } finally {
                isInitializing = false
            }
        }
    }

    /**
     * 借鉴官方：使用 Session 进行流式或完整对话
     */
    fun getResponse(prompt: String): String {
        val session = llmSession ?: return "引擎未就绪"
        return try {
            // 官方推荐用法：将 Query 加入 Session
            session.addQueryChunk(prompt)
            val result = session.generateResponse() 
            if (result.isNullOrBlank()) "..." else result
        } catch (e: Exception) {
            "推理崩溃: ${e.localizedMessage}"
        }
    }

    fun close() {
        llmSession?.close()
        llmInference?.close()
        llmSession = null
        llmInference = null
        isInitializing = false
    }

    private fun showError(context: Context, msg: String) = mainHandler.post {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
