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
                // 1. 获取系统提供的私有路径
                val folder = context.getExternalFilesDir(null)
                
                // 2. 核心修正：确保路径是以物理挂载点 /storage/emulated/0 开头
                // 如果系统返回了 /sdcard/，我们将其强行替换为物理地址
                var basePath = folder?.absolutePath ?: "/storage/emulated/0/Android/data/com.example.hello/files"
                
                if (basePath.contains("/sdcard/")) {
                    basePath = basePath.replace("/sdcard/", "/storage/emulated/0/")
                }

                // 3. 锁定文件名
                val fileName = "gemma-4-E2B-it.litertlm"
                val modelFile = File(basePath, fileName)

                Log.d(TAG, "🎯 最终锁定的物理路径: ${modelFile.absolutePath}")

                // 4. 物理文件存在性与大小检查
                if (modelFile.exists() && modelFile.canRead()) {
                    Log.d(TAG, "✅ 文件确认就绪，大小: ${modelFile.length()} 字节")

                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    // 5. 启动 MediaPipe 引擎
                    llmInference = LlmInference.createFromOptions(context, options)
                    callback(llmInference != null)
                } else {
                    Log.e(TAG, "❌ 找不到物理文件！请检查路径：${modelFile.absolutePath}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 引擎启动崩溃: ${e.message}")
                e.printStackTrace()
                close()
                callback(false)
            }
        }
    }

    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎未就绪"
        } catch (e: Exception) {
            "推理错误: ${e.message}"
        }
    }

    fun close() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(TAG, "释放资源异常: ${e.message}")
        }
        llmInference = null
    }
}
