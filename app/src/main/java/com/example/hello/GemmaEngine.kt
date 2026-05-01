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
                // 自查点 1：确保路径获取准确
                val folder = context.getExternalFilesDir(null)
                val modelFile = folder?.listFiles()?.find { it.name.contains("gemma", ignoreCase = true) }

                if (modelFile != null && modelFile.exists()) {
                    // 自查点 2：MediaPipe GenAI 库在加载外部文件时
                    // 必须使用 setModelPath 而不是 setModelAssetPath
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath) 
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    llmInference = LlmInference.createFromOptions(context, options)
                    
                    // 自查点 3：确保回调在主线程之前完成逻辑判断
                    val success = llmInference != null
                    callback(success)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
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
            e.printStackTrace()
        }
        llmInference = null
    }
}
