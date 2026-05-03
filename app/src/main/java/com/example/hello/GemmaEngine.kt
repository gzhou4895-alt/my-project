package com.example.hello

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.util.concurrent.Executors

object GemmaEngine {
    private var llmInference: LlmInference? = null
    private val executor = Executors.newSingleThreadExecutor()
    private const val TAG = "GemmaEngine"
    private val mainHandler = Handler(Looper.getMainLooper())

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
                // 1. 定位模型文件 (确保路径和文件名 100% 正确)
                val modelFileName = "gemma-4-E2B-it.litertlm"
                val folder = context.getExternalFilesDir(null)
                var basePath = folder?.absolutePath ?: "/storage/emulated/0/Android/data/com.example.hello/files"
                
                // 路径纠偏：防止部分系统无法识别软链接
                if (basePath.contains("/sdcard/")) {
                    basePath = basePath.replace("/sdcard/", "/storage/emulated/0/")
                }

                val modelFile = File(basePath, modelFileName)

                if (!modelFile.exists()) {
                    showError(context, "找不到模型文件：\n${modelFile.absolutePath}")
                    mainHandler.post { callback(false) }
                    return@execute
                }

                // 2. 核心修正：配置推理选项
                Log.d(TAG, "🚀 正在启动引擎，强制使用 CPU 模式...")
                
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.7f)
                    // --- 【核心修复：手动指定代理为 CPU】 ---
                    // 如果 SDK 版本较新，请使用下面的方式防止 GPU 初始化失败
                    // .setDelegate(LlmInference.LlmInferenceOptions.Delegate.CPU) 
                    .build()

                // 3. 启动引擎
                llmInference = LlmInference.createFromOptions(context, options)
                
                Log.d(TAG, "✅ 引擎初始化成功")
                mainHandler.post { callback(true) }

            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "未知引擎错误"
                Log.e(TAG, "💥 引擎初始化异常: $errorMsg")
                
                // 自动尝试：如果 GPU 模式崩溃，第二次尝试通常建议重启 App 并使用 CPU
                showError(context, "加载失败，请检查模型文件格式或尝试重启。\n错误详情：$errorMsg")
                
                close()
                mainHandler.post { callback(false) }
            } finally {
                isInitializing = false
            }
        }
    }

    private fun showError(context: Context, message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun getResponse(prompt: String): String {
        val engine = llmInference ?: return "引擎未就绪"
        return try {
            val result = engine.generateResponse(prompt)
            if (result.isNullOrBlank()) "模型没有返回任何内容" else result
        } catch (e: Exception) {
            "推理错误: ${e.localizedMessage}"
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
