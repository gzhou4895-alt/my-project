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

    // 增加一个状态锁，防止多次重复初始化
    @Volatile
    private var isInitializing = false

    fun isReady(): Boolean = llmInference != null

    fun initialize(context: Context, callback: (Boolean) -> Unit) {
        if (llmInference != null) {
            callback(true)
            return
        }
        
        if (isInitializing) return
        isInitializing = true

        executor.execute {
            try {
                // 1. 路径处理：直接获取 App 专有的外部文件目录
                val folder = context.getExternalFilesDir(null)
                var basePath = folder?.absolutePath ?: "/storage/emulated/0/Android/data/com.example.hello/files"
                
                // 统一路径格式，避免符号链接导致的 SDK 读取失败
                if (basePath.contains("/sdcard/")) {
                    basePath = basePath.replace("/sdcard/", "/storage/emulated/0/")
                }

                val fileName = "gemma-4-E2B-it.litertlm"
                val modelFile = File(basePath, fileName)

                Log.d(TAG, "🎯 检查路径: ${modelFile.absolutePath}")

                // 2. 严格检查文件
                if (modelFile.exists() && modelFile.length() > 1024 * 1024) { // 至少大于1MB
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    // 3. 实例化引擎
                    val inference = LlmInference.createFromOptions(context, options)
                    llmInference = inference
                    
                    Log.d(TAG, "✅ 引擎实例已创建")
                    callback(true)
                } else {
                    Log.e(TAG, "❌ 文件不存在或损坏: ${modelFile.absolutePath}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 引擎加载失败: ${e.message}")
                close()
                callback(false)
            } finally {
                isInitializing = false
            }
        }
    }

    /**
     * 获取回复，增加了一些容错处理
     */
    fun getResponse(prompt: String): String {
        val engine = llmInference ?: return "错误：引擎尚未初始化。"
        return try {
            // MediaPipe 的 generateResponse 是同步阻塞的，必须在子线程运行
            val result = engine.generateResponse(prompt)
            if (result.isNullOrBlank()) "（模型未返回任何内容）" else result
        } catch (e: Exception) {
            Log.e(TAG, "推理崩溃: ${e.message}")
            "推理出错: ${e.localizedMessage}"
        }
    }

    fun close() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭异常: ${e.message}")
        }
        llmInference = null
        isInitializing = false
    }
}
