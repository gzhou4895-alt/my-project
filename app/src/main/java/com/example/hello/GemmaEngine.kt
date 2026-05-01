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
                val modelFile = File(folder, "gemma-4-E2B-it.litertlm")
                
                if (!modelFile.exists()) {
                    // 自动匹配目录下任何包含 gemma 的文件，防止文件名微差
                    val files = folder?.listFiles()
                    val foundFile = files?.find { it.name.contains("gemma", ignoreCase = true) }
                    
                    if (foundFile != null) {
                        setupEngine(context, foundFile, callback)
                    } else {
                        callback(false)
                    }
                } else {
                    setupEngine(context, modelFile, callback)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    // 注意：这个方法现在是在 initialize 外部定义的
    private fun setupEngine(context: Context, file: File, callback: (Boolean) -> Unit) {
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                // 如果运行依然失败，请试着把下面这行 .setDelegate 删掉，先用 CPU 测试
                .setDelegate(LlmInference.LlmInferenceOptions.Delegate.GPU)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.7f)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

    // 统一方法名，确保 ChatFragment 能找到
    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎尚未准备好"
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
