package com.example.hello;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.net.InetSocketAddress;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class LiteRTService extends Service {
    private static final String TAG = "LiteRTServiceV2";
    private static final int PORT = 8080;
    private HttpServer server;
    private static final String MODEL_PATH = "/sdcard/Download/gemma4_2b_v09_obfus_fix_all_modalities_thinking.litertlm";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate started");
        
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/v1/chat/completions", new ChatHandler());
            server.setExecutor(null);
            server.start();
            Log.e(TAG, "HTTP server started on port " + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start server", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop(0);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if (!"POST".equals(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    bodyBuilder.append(line);
                }
                String body = bodyBuilder.toString();

                JsonObject request = JsonParser.parseString(body).getAsJsonObject();
                JsonObject message = request.getAsJsonArray("messages")
                        .get(request.getAsJsonArray("messages").size() - 1)
                        .getAsJsonObject();
                String prompt = message.get("content").getAsString();

                // 这里是模拟回复，因为云编译无法运行 litert-lm
                String responseText = "这是来自云编译的测试回复。实际部署时请替换为真实的 litert-lm 调用。";

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("id", "chatcmpl-" + System.currentTimeMillis());
                responseJson.addProperty("object", "chat.completion");
                responseJson.addProperty("created", System.currentTimeMillis() / 1000);
                responseJson.addProperty("model", "gemma-4-e2b");
                JsonObject choice = new JsonObject();
                choice.addProperty("index", 0);
                JsonObject msg = new JsonObject();
                msg.addProperty("role", "assistant");
                msg.addProperty("content", responseText);
                choice.add("message", msg);
                choice.addProperty("finish_reason", "stop");
                responseJson.add("choices", new com.google.gson.JsonArray());
                responseJson.getAsJsonArray("choices").add(choice);

                String response = responseJson.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                String error = "{\"error\": \"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }
    }
}
