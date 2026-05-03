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
                // 1. 获取物理路径（尝试多个可能的位置）
                val modelFileName = "gemma-4-E2B-it.litertlm"
                val possibleFolders = listOf(
                    context.getExternalFilesDir(null), // /storage/emulated/0/Android/data/com.example.hello/files
                    context.filesDir // /data/user/0/com.example.hello/files
                )

                var modelFile: File? = null
                for (folder in possibleFolders) {
                    if (folder == null) continue
                    var path = folder.absolutePath
                    // 路径纠偏
                    if (path.contains("/sdcard/")) {
                        path = path.replace("/sdcard/", "/storage/emulated/0/")
                    }
                    val target = File(path, modelFileName)
                    Log.d(TAG, "🔍 正在检查路径: ${target.absolutePath}")
                    if (target.exists()) {
                        modelFile = target
                        break
                    }
                }

                // 2. 检查文件状态
                if (modelFile == null || !modelFile.exists()) {
                    showError(context, "找不到模型文件！请确保文件放在 Android/data/com.example.hello/files/ 目录下，且文件名为 $modelFileName")
                    mainHandler.post { callback(false) }
                    return@execute
                }

                if (modelFile.length() < 100 * 1024 * 1024) { // 小于100MB肯定不对
                    showError(context, "模型文件损坏或不完整（当前大小: ${modelFile.length() / 1024 / 1024}MB）")
                    mainHandler.post { callback(false) }
                    return@execute
                }

                // 3. 配置引擎
                Log.d(TAG, "🚀 正在从以下路径加载引擎: ${modelFile.absolutePath}")
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.7f)
                    .build()

                // 4. 创建实例（这是最容易报错的地方）
                llmInference = LlmInference.createFromOptions(context, options)
                
                Log.d(TAG, "✅ 引擎加载成功")
                mainHandler.post { callback(true) }

            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "未知引擎错误"
                Log.e(TAG, "💥 启动崩溃: $errorMsg")
                showError(context, "引擎加载失败: $errorMsg")
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
        val engine = llmInference ?: return "错误：引擎尚未初始化"
        return try {
            val result = engine.generateResponse(prompt)
            if (result.isNullOrBlank()) "（模型未返回内容）" else result
        } catch (e: Exception) {
            Log.e(TAG, "推理错误: ${e.message}")
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
