package com.example.hello

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.core.Delegate // 显式导入 Delegate
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
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.7f)
                    // 修正后的 Delegate 设置方式
                    .setResultListener { _, _ -> } // 必须设置，即使为空
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
        return llmInference?.generateResponse(prompt) ?: "Engine not ready"
    }
}
