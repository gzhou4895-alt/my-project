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
        // 1. 获取 Android 标准的 files 目录
        val folder = context.getExternalFilesDir(null)
        val modelFile = File(folder, "gemma-4-E2B-it.litertlm")
        
        // 打印出代码实际寻找的路径（你可以在 Logcat 中看到它）
        println("AI_DEBUG: 正在寻找模型: ${modelFile.absolutePath}")

        if (!modelFile.exists()) {
            // 2. 备选方案：如果上面的路径找不到，尝试列出目录下所有文件，看看是不是文件名多了后缀
            val files = folder?.listFiles()
            val foundFile = files?.find { it.name.contains("gemma", ignoreCase = true) }
            
            if (foundFile != null) {
                println("AI_DEBUG: 自动匹配到文件: ${foundFile.absolutePath}")
                setupEngine(context, foundFile, callback)
            } else {
                println("AI_DEBUG: 目录下没有任何包含 gemma 的文件")
                callback(false)
            }
            return@execute
        }

        // 3. 文件存在，正常初始化
        setupEngine(context, modelFile, callback)

    } catch (e: Exception) {
        println("AI_DEBUG: 引擎启动发生崩溃: ${e.message}")
        e.printStackTrace()
        callback(false)
    }
}

// 提取出的配置方法
private fun setupEngine(context: Context, file: File, callback: (Boolean) -> Unit) {
    try {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(file.absolutePath)
            // 如果 GPU 报错，请尝试将下面这行改为 .setDelegate(LlmInference.LlmInferenceOptions.Delegate.CPU)
            .setDelegate(LlmInference.LlmInferenceOptions.Delegate.GPU)
            .setMaxTokens(1024)
            .setTopK(40)
            .setTemperature(0.7f)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        callback(true)
    } catch (e: Exception) {
        callback(false)
    }
}
