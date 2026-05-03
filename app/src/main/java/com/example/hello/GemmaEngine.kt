package com.example.hello

import android.content.Context
import android.os.Environment
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
                // 1. 暴力探测所有可能的模型存放路径
                val modelFile = findModelFile(context)

                if (modelFile != null && modelFile.exists()) {
                    Log.d(TAG, "找到模型文件: ${modelFile.absolutePath}")

                    // 2. 配置 MediaPipe 参数
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    // 3. 初始化引擎（核心耗时操作）
                    llmInference = LlmInference.createFromOptions(context, options)
                    
                    val success = llmInference != null
                    Log.d(TAG, "引擎初始化结果: $success")
                    callback(success)
                } else {
                    Log.e(TAG, "在所有探测路径下均未找到包含 'gemma' 的模型文件")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "初始化异常: ${e.message}")
                e.printStackTrace()
                close()
                callback(false)
            }
        }
    }

    /**
     * 自动探测路径逻辑：兼容标准路径、截图路径和下载目录
     */
    private fun findModelFile(context: Context): File? {
        val root = Environment.getExternalStorageDirectory()
        
        val possibleFolders = listOf(
            context.getExternalFilesDir(null), // 标准私有: /Android/data/com.example.hello/files/
            File(root, "Android/data/com.example.hello/files"), // 手写强制路径
            File(root, "example.hello/files"), // 针对你截图显示的路径: /example.hello/files/
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) // 公共下载区
        )

        for (folder in possibleFolders) {
            if (folder != null && folder.exists()) {
                val found = folder.listFiles()?.find { 
                    it.name.contains("gemma", ignoreCase = true) && 
                    (it.name.endsWith(".bin") || it.name.endsWith(".task") || it.name.endsWith(".litertlm"))
                }
                if (found != null) return found
            }
        }
        return null
    }

    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎未就绪"
        } catch (e: Exception) {
            Log.e(TAG, "推理错误: ${e.message}")
            "推理失败: ${e.localizedMessage}"
        }
    }

    fun close() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭引擎异常: ${e.message}")
        }
        llmInference = null
    }
}
