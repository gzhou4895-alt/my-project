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
                
                // 智能检测：如果文件名对不上，尝试自动匹配目录下任何包含 gemma 的文件
                val finalFile = if (modelFile.exists()) {
                    modelFile
                } else {
                    folder?.listFiles()?.find { it.name.contains("gemma", ignoreCase = true) }
                }

                if (finalFile != null && finalFile.exists()) {
                    setupEngine(context, finalFile, callback)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    private fun setupEngine(context: Context, file: File, callback: (Boolean) -> Unit) {
        try {
            // 使用最稳健的配置方式
            val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.7f)

            // 注意：如果编译依然报 Delegate 错误，直接删除下面这行 .setResultListener 及其后的逻辑
            // 某些版本的 MediaPipe 会根据硬件自动选择最快的 Delegate (通常就是 GPU)
            optionsBuilder.setResultListener { _, _ -> } 

            llmInference = LlmInference.createFromOptions(context, optionsBuilder.build())
            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }

    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎未就绪"
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
