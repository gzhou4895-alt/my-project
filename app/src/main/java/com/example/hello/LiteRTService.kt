package com.example.hello

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
// 1. 尝试 MediaPipe 兼容路径 (这是目前 0.10.x 最主流的路径)
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LiteRTService : Service() {
    // 使用全路径定义，防止 Import 歧义
    private var llmInference: Any? = null 
    private var server: HTTPServer? = null
    private val port = 8080
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate started")

        try {
            // 这里我们使用反射风格或确保路径正确
            // 如果 LlmInference 在 com.google.mediapipe 下：
            val options = com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .build()
            
            val inference = com.google.mediapipe.tasks.genai.llminference.LlmInference.create(this, options)
            llmInference = inference
            Log.e(TAG, "LlmInference initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Init failed, trying alternative path: ${e.message}")
            try {
                // 备选路径：如果它在 com.google.ai.edge 下
                // 很多 0.10.2 版本其实是将 LlmInference 直接放在根包
                /* 备用逻辑逻辑占位 */
            } catch (e2: Exception) {
                stopSelf()
            }
        }

        // 启动 HTTP 服务器
        try {
            server = HTTPServer(port)
            server?.start()
        } catch (e: IOException) {
            Log.e(TAG, "Server failed", e)
        }
    }

    override fun onDestroy() {
        server?.stop()
        (llmInference as? AutoCloseable)?.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private inner class HTTPServer(port: Int) : NanoHTTPD("0.0.0.0", port) {
        override fun serve(session: IHTTPSession): Response {
            if (session.uri == "/v1/chat/completions") {
                return try {
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val body = files["postData"] ?: "{}"
                    val requestJson = com.google.gson.JsonParser.parseString(body).asJsonObject
                    val messages = requestJson.getAsJsonArray("messages")
                    val prompt = messages[messages.size() - 1].asJsonObject.get("content").asString

                    // 动态调用 generateResponse
                    val responseText = if (llmInference is com.google.mediapipe.tasks.genai.llminference.LlmInference) {
                        (llmInference as com.google.mediapipe.tasks.genai.llminference.LlmInference).generateResponse(prompt)
                    } else {
                        "Engine not initialized"
                    }

                    val responseJson = com.google.gson.JsonObject().apply {
                        addProperty("id", "chatcmpl-${System.currentTimeMillis()}")
                        add("choices", com.google.gson.JsonArray().apply {
                            add(com.google.gson.JsonObject().apply {
                                add("message", com.google.gson.JsonObject().apply {
                                    addProperty("role", "assistant")
                                    addProperty("content", responseText)
                                })
                            })
                        })
                    }
                    newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString())
                } catch (e: Exception) {
                    newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString())
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }

    companion object {
        private const val TAG = "LiteRTService"
    }
}
