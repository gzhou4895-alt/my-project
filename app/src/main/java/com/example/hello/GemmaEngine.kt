package com.example.hello

import android.content.Context
import android.os.Build
import android.os.Environment
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
                // 1. 物理权限硬核检查：如果没权限，直接断开，绝不调用 MediaPipe 接口
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    callback(false)
                    return@execute
                }

                // 2. 安全获取目录，增加所有环节的空判断
                val folder = context.getExternalFilesDir(null)
                if (folder == null || !folder.exists()) {
                    callback(false)
                    return@execute
                }

                // 3. 安全寻找文件
                val files = folder.listFiles()
                val modelFile = files?.find { it.name.contains("gemma", ignoreCase = true) }

                if (modelFile != null && modelFile.exists()) {
                    // 4. MediaPipe 的最后防线：构建 Options
                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.7f)
                        .build()

                    // 这里是闪退高发区，MediaPipe 内部加载异常
                    llmInference = LlmInference.createFromOptions(context, options)
                    callback(llmInference != null)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 彻底释放，防止内存残留导致下次启动依然崩
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
            // 忽略关闭异常
        }
        llmInference = null
    }
}
