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
                val modelFile = File(context.getExternalFilesDir(null), "gemma-4-E2B-it.litertlm")
                if (!modelFile.exists()) {
                    callback(false)
                    return@execute
                }

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    // 开启 GPU 加速
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
    }

    fun getResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "引擎未初始化"
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
