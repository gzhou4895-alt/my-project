package com.example.hello;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class LiteRTService extends Service {
    private static final String TAG = "LiteRTService";
    private static final int PORT = 8080;
    private HttpServer server;

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
            String response = "{\"message\":\"Hello from LiteRT Adapter\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
