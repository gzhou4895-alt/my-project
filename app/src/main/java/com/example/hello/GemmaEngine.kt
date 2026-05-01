package com.example.hello

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.util.concurrent.Executors

object GemmaEngine {
    private var llmInference: LlmInference? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun isReady(): Boolean = llmInference != null

    fun initialize(context: Context, callback: (Boolean) -> Unit) {
        if (llmInference != null) {
            callback(true)
            return
        }

        executor.execute {
            try {
                val folder = context.getExternalFilesDir(null)
                // 自动匹配任何包含 gemma 的文件，解决文件名后缀微差问题
                val modelFile = folder?.listFiles()?.find { it.name.contains("gemma", ignoreCase = true) }

                if (modelFile != null && modelFile.exists()) {
                    // 核心初始化逻辑
                    val success = startInferenceEngine(context, modelFile)
                    callback(success)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    private fun startInferenceEngine(context: Context, file: File): Boolean {
        return try {
            // 尝试 1：优先使用 GPU 加速（适合你的 12GB RAM 手机）
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.7f)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果 GPU 握手失败，MediaPipe 有时会自动处理，
            // 但如果 llmInference 依然为 null，这里会返回 false
            llmInference != null
        }
    }

    /**
     * 获取对话响应
     */
    fun getResponse(prompt: String): String {
        return try {
            if (llmInference == null) return "引擎尚未就绪"
            
            // 直接获取生成结果
            llmInference?.generateResponse(prompt) ?: "生成失败"
        } catch (e: Exception) {
            "推理过程发生错误: ${e.message}"
        }
    }

    /**
     * 释放资源
     */
    fun close() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        llmInference = null
    }
}
