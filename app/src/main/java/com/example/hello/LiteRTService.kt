package com.example.hello

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
// 彻底弃用 mediapipe 路径，改用全新的 LiteRT 路径
import com.google.ai.edge.litert.tasks.genai.llminference.LlmInference
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LiteRTService : Service() {
    
    private var llmInference: LlmInference? = null 
    private var server: HTTPServer? = null
    private val port = 8080
    // 注意：这里的模型后缀建议检查，0.10.x 版本通常配合新的 .bin 或 .task
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate started")

        try {
            // 使用全新的 LiteRT 路径配置 Options
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .build()
            
            // 使用 context 和 options 初始化
            llmInference = LlmInference.create(this, options)
            Log.i(TAG, "LiteRT LlmInference initialized")
        } catch (e: Exception) {
            Log.e(TAG, "LiteRT Init failed: ${e.message}")
            stopSelf()
        }

        // 启动 HTTP 服务器
        try {
            server = HTTPServer(port)
            server?.start()
            Log.i(TAG, "Server started on port $port")
        } catch (e: IOException) {
            Log.e(TAG, "Server failed to start", e)
        }
    }

    override fun onDestroy() {
        server?.stop()
        llmInference?.close()
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

                    // 注意：LiteRT 0.10.x 版本中方法名可能已更改为 generate
                    // 如果 generate(prompt) 报错，请尝试使用 generateResponse(prompt)
                    val responseText = llmInference?.generate(prompt) ?: "Engine not initialized"

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
                    Log.e(TAG, "Processing error", e)
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
