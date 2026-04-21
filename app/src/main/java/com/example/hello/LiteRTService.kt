package com.example.hello

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
// 使用 LiteRT 0.10.2 的新路径
import com.google.ai.edge.litert.tasks.genai.llminference.LlmInference
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LiteRTService : Service() {
    
    private var llmInference: LlmInference? = null 
    private var server: HTTPServer? = null
    private val port = 8080
    private val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate started")

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .build()
            
            llmInference = LlmInference.create(this, options)
            Log.i(TAG, "LiteRT LlmInference initialized")
        } catch (e: Exception) {
            Log.e(TAG, "LiteRT Init failed: ${e.message}")
            stopSelf()
        }

        try {
            server = HTTPServer(port)
            server?.start()
            Log.i(TAG, "Server started on $port")
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

                    // 0.10.2 版方法名已从 generateResponse 简化为 generate
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
