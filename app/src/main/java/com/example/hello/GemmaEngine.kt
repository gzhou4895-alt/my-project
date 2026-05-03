package com.example.hello

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.util.concurrent.Executors

object GemmaEngine {
    private var llmInference: LlmInference? = null
    private val executor = Executors.newSingleThreadExecutor()
    private const val TAG = "GemmaEngine"

    fun isReady(): Boolean = llmInference != null

    fun initialize(context: Context, callback: (Boolean) -> Unit) {
        if (llmInference != null) {
            callback(true)
            return
        }

        executor.execute {
            try {
                // 1. 路径严格对齐官方 Demo
                val folder = context.getExternalFilesDir(null)
                val modelFile = File(folder, "gemma-4-E2B-it.litertlm")

                if (!modelFile.exists()) {
                    Log.e(TAG, "找不到模型文件")
                    callback(false)
                    return@execute
                }

                // 2. 借鉴点：使用与官方 Demo 一致的底层配置
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    // 核心参数：根据 2026 SDK 最佳实践调整
                    .setMaxTokens(1024) 
                    .setTopK(40)
                    .setTemperature(0.7f)
                    .setRandomSeed(42)
                    .build()

                // 3. 官方 Demo 实际上是在初始化时预热了算子
                llmInference = LlmInference.createFromOptions(context, options)
                
                // 预热 (Warm-up)：这是丝滑的关键，先跑一个空格，让 GPU 分配好内存
                llmInference?.generateResponse(" ") 

                Log.d(TAG, "✅ 引擎预热完成")
                callback(true)
            } catch (e: Exception) {
                Log.e(TAG, "💥 初始化失败: ${e.message}")
                callback(false)
            }
        }
    }

    /**
     * 同步获取结果
     */
    fun getResponse(prompt: String): String {
        return try {
            val result = llmInference?.generateResponse(prompt)
            if (result.isNullOrBlank()) "..." else result
        } catch (e: Exception) {
            "推理出错: ${e.localizedMessage}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
