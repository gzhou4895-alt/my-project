package com.example.hello

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.ai.edge.litertlm.LlmInference
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LiteRTService : Service() {
    private var llmInference: LlmInference? = null
    private var server: HTTPServer? = null
    private val port = 8080
    // 确保这个路径在你的手机上是正确的
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate started")

        // 1. 初始化推理引擎 (官方 LlmInference 方式)
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTopK(40)
                .setTemperature(0.7f)
                .build()
            
            // 注意：第一个参数是 Context
            llmInference = LlmInference.create(this, options)
            Log.e(TAG, "LlmInference initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize LlmInference", e)
            stopSelf()
            return
        }

        // 2. 启动 HTTP 服务器
        try {
            server = HTTPServer(port)
            server?.start()
            Log.e(TAG, "HTTP server started on port $port")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
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

                    // 调用推理
                    val responseText = runInference(prompt)

                    val responseJson = com.google.gson.JsonObject().apply {
                        addProperty("id", "chatcmpl-${System.currentTimeMillis()}")
                        add("choices", com.google.gson.JsonArray().apply {
                            add(com.google.gson.JsonObject().apply {
                                add("message", com.google.gson.JsonObject().apply {
                                    addProperty("role", "assistant")
                                    addProperty("content", responseText)
                                })
                                addProperty("finish_reason", "stop")
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

        private fun runInference(prompt: String): String {
            return try {
                // 核心：官方 generateResponse 方法
                val result = llmInference?.generateResponse(prompt)
                result ?: "Empty response"
            } catch (e: Exception) {
                Log.e(TAG, "Inference failed", e)
                "Error: ${e.message}"
            }
        }
    }

    companion object {
        private const val TAG = "LiteRTService"
    }
}
