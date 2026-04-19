package com.example.hello;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import fi.iki.elonen.NanoHTTPD;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LiteRTService extends Service {
    private static final String TAG = "LiteRTService";
    private static final int PORT = 8080;
    private HTTPServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate started");
        
        try {
            server = new HTTPServer(PORT);
            server.start();
            Log.e(TAG, "HTTP server started on port " + PORT);
        } catch (IOException e) {
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
            server.stop();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class HTTPServer extends NanoHTTPD {
        public HTTPServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            Log.e(TAG, "Request: " + uri);
            
            if ("/v1/chat/completions".equals(uri)) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String body = files.get("postData");
                    
                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("id", "chatcmpl-" + System.currentTimeMillis());
                    responseJson.addProperty("object", "chat.completion");
                    responseJson.addProperty("created", System.currentTimeMillis() / 1000);
                    responseJson.addProperty("model", "gemma-4-e2b");
                    
                    JsonObject choice = new JsonObject();
                    choice.addProperty("index", 0);
                    JsonObject msg = new JsonObject();
                    msg.addProperty("role", "assistant");
                    msg.addProperty("content", "这是来自 LiteRT Adapter 的测试回复。");
                    choice.add("message", msg);
                    choice.addProperty("finish_reason", "stop");
                    
                    com.google.gson.JsonArray choices = new com.google.gson.JsonArray();
                    choices.add(choice);
                    responseJson.add("choices", choices);
                    
                    return newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error", e);
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        }
    }
}
