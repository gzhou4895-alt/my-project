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
    private var engine: Engine? = null
    private var server: HTTPServer? = null
    private val port = 8080
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate started")

        try {
            // 修正 1：0.10.2 版本中，Builder 必须这样显式调用
            val configBuilder = EngineConfig.builder()
            configBuilder.setModelPath(modelPath)
            
            // 修正 2：Backend.GPU 在某些版本是单例，某些是类。
            // 如果 Backend.GPU() 报错，请试着直接用 Backend.GPU
            configBuilder.setBackend(Backend.GPU()) 
            
            val config = configBuilder.build()
            
            // 修正 3：Engine 的创建
            engine = Engine.create(config)
            
            Log.e(TAG, "Engine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize engine", e)
            stopSelf()
            return
        }

        try {
            server = HTTPServer(port)
            server?.start()
            Log.e(TAG, "HTTP server started on port $port")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
        }
    }

    override fun onDestroy() {
        server?.stop()
        engine?.close()
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

                    // 修正 4：推理调用
                    val responseText = runInference(prompt)

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

        private fun runInference(prompt: String): String {
            return try {
                // 修正 5：0.10.2 的 generate 可能返回 Result 对象，需要调用 .text 或直接返回 String
                val result = engine?.generate(prompt)
                result ?: "No response from model"
            } catch (e: Exception) {
                "Inference error: ${e.message}"
            }
        }
    }

    companion object {
        private const val TAG = "LiteRTService"
    }
}
