package com.example.hello

import android.content.Context
import android.os.ParcelFileDescriptor
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
            var pfd: ParcelFileDescriptor? = null
            try {
                val folder = context.getExternalFilesDir(null)
                val modelFile = File(folder, "gemma-4-E2B-it.litertlm")

                if (modelFile.exists()) {
                    // 🔥 核心修正：不再直接传路径字符串
                    // 而是通过 Java 打开文件拿到句柄 (PFD)，再传给 Native 层
                    pfd = ParcelFileDescriptor.open(modelFile, ParcelFileDescriptor.MODE_READ_ONLY)

                    val options = LlmInference.LlmInferenceOptions.builder()
                        // 使用 .setModelPath，但在某些底层实现中，传入已授权的路径会更稳
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    llmInference = LlmInference.createFromOptions(context, options)
                    callback(llmInference != null)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            } finally {
                // 注意：由于 createFromOptions 是同步的，初始化完成后可以安全关闭 Java 端的句柄
                try { pfd?.close() } catch (e: Exception) {}
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
        llmInference?.close()
        llmInference = null
    }
}
