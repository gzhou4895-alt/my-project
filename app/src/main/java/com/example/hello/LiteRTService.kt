package com.example.hello

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Backend
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LiteRTService : Service() {
    private lateinit var engine: Engine
    private var server: HTTPServer? = null
    private val port = 8080
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate started")

        // 初始化推理引擎
        try {
            val config = EngineConfig.builder()
                .setModelPath(modelPath)
                .setBackend(Backend.GPU)
                .build()
            engine = Engine.create(config)
            Log.e(TAG, "Engine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize engine", e)
            stopSelf()
            return
        }

        // 启动 HTTP 服务器
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
        engine.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private inner class HTTPServer(port: Int) : NanoHTTPD("0.0.0.0", port) {
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            Log.e(TAG, "Request: $uri")

            if (uri == "/v1/chat/completions") {
                return try {
                    val files = HashMap<String, String>()
                    session.parseBody(files)
                    val body = files["postData"] ?: "{}"

                    val requestJson = com.google.gson.JsonParser.parseString(body).asJsonObject
                    val messages = requestJson.getAsJsonArray("messages")
                    val lastMessage = messages[messages.size() - 1].asJsonObject
                    val prompt = lastMessage.get("content").asString

                    val responseText = runInference(prompt)

                    val responseJson = com.google.gson.JsonObject().apply {
                        addProperty("id", "chatcmpl-${System.currentTimeMillis()}")
                        addProperty("object", "chat.completion")
                        addProperty("created", System.currentTimeMillis() / 1000)
                        addProperty("model", "gemma-4-e2b")
                        add("choices", com.google.gson.JsonArray().apply {
                            add(com.google.gson.JsonObject().apply {
                                addProperty("index", 0)
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
                    Log.e(TAG, "Error processing request", e)
                    newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString())
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }

        private fun runInference(prompt: String): String {
            return try {
                val response = engine.generate(prompt)
                response.text
            } catch (e: Exception) {
                Log.e(TAG, "Inference failed", e)
                "模型推理失败: ${e.message}"
            }
        }
    }

    companion object {
        private const val TAG = "LiteRTService"
    }
}
