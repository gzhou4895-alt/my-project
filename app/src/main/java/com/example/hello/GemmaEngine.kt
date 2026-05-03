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

    // 关键：定义一个回调，让结果能传回 Fragment
    private var resultListener: ((String, Boolean) -> Unit)? = null

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
                    Log.e(TAG, "找不到模型文件: ${modelFile.absolutePath}")
                    callback(false)
                    return@execute
                }

                // 使用最基础的 Options，确保所有版本 SDK 都能编译通过
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.7f)
                    // 设置流式监听器
                    .setResultListener { result, done ->
                        resultListener?.invoke(result, done)
                    }
                    .build()

                llmInference = LlmInference.createFromOptions(context, options)
                
                // 预热：避免第一次说话卡顿
                try { llmInference?.generateResponse(" ") } catch (e: Exception) {}

                Log.d(TAG, "✅ 引擎初始化成功")
                callback(true)
            } catch (e: Exception) {
                Log.e(TAG, "💥 初始化失败: ${e.message}")
                callback(false)
            }
        }
    }

    /**
     * 设置监听器（供 Fragment 调用）
     */
    fun setOnResultListener(listener: (String, Boolean) -> Unit) {
        this.resultListener = listener
    }

    /**
     * 模仿官方的异步发送：不阻塞主线程，不报 RET_CHECK
     */
    fun sendPrompt(prompt: String) {
        executor.execute {
            try {
                if (llmInference == null) {
                    resultListener?.invoke("引擎未就绪", true)
                    return@execute
                }
                // 使用异步接口，这是丝滑的关键
                llmInference?.generateResponseAsync(prompt)
            } catch (e: Exception) {
                Log.e(TAG, "发送失败: ${e.message}")
                resultListener?.invoke("出错: ${e.localizedMessage}", true)
            }
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
