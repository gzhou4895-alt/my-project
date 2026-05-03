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
                // 1. 获取 App 安装时自动创建的标准私有目录
                // 对应路径：/storage/emulated/0/Android/data/com.example.hello/files
                val folder = context.getExternalFilesDir(null)
                
                // 2. 这里的模型文件名必须和你放入文件夹的文件名完全一致
                val fileName = "gemma-4-E2B-it.litertlm"
                val modelFile = File(folder, fileName)

                Log.d(TAG, "正在尝试读取模型: ${modelFile.absolutePath}")

                // 3. 核心检查：不仅检查是否存在，还要检查是否可读，以及大小是否正确
                if (modelFile.exists() && modelFile.canRead()) {
                    Log.d(TAG, "文件确认成功，大小: ${modelFile.length()} 字节")

                    // 4. 配置 LlmInference
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath) // 必须是绝对路径
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    // 5. 创建实例（如果此处闪退，请确认 AndroidManifest 中开启了 largeHeap）
                    llmInference = LlmInference.createFromOptions(context, options)
                    
                    val success = llmInference != null
                    Log.d(TAG, "MediaPipe 引擎创建结果: $success")
                    callback(success)
                } else {
                    Log.e(TAG, "文件不存在或无读取权限！请确认模型已放入: ${modelFile.absolutePath}")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "初始化过程发生崩溃: ${e.message}")
                e.printStackTrace()
                close()
                callback(false)
            }
        }
    }

    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎未就绪，请检查模型文件"
        } catch (e: Exception) {
            Log.e(TAG, "推理出错: ${e.message}")
            "AI 思考时出错了: ${e.localizedMessage}"
        }
    }

    fun close() {
        try {
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭异常: ${e.message}")
        }
        llmInference = null
    }
}

