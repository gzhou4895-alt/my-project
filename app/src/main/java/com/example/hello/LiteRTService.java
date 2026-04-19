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
import com.google.gson.JsonParser;

public class LiteRTService extends Service {
    private static final String TAG = "LiteRTService";
    private static final int PORT = 8080;
    private HTTPServer server;
    private static final String MODEL_PATH = "/sdcard/Download/gemma-4-E2B-it.litertlm";
    private static final String LOG_FILE = "/sdcard/Download/litert_adapter_debug.log";

    private void writeFileLog(String msg) {
        try {
            FileWriter fw = new FileWriter(LOG_FILE, true);
            fw.append(msg + "\n");
            fw.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        writeFileLog("=== onCreate started ===");
        
        try {
            server = new HTTPServer(PORT);
            server.start();
            writeFileLog("HTTP server started on port " + PORT);
        } catch (IOException e) {
            writeFileLog("Failed to start server: " + e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        writeFileLog("onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        writeFileLog("onDestroy");
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
        public HTTPServer(int port) throws IOException {
            super("0.0.0.0", port);
            writeFileLog("HTTPServer constructor");
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            writeFileLog("Request received: " + uri);
            
            if ("/v1/chat/completions".equals(uri)) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String body = files.get("postData");
                    writeFileLog("Request body: " + body);
                    
                    JsonObject request = JsonParser.parseString(body).getAsJsonObject();
                    JsonObject message = request.getAsJsonArray("messages")
                            .get(request.getAsJsonArray("messages").size() - 1)
                            .getAsJsonObject();
                    String prompt = message.get("content").getAsString();
                    writeFileLog("Prompt: " + prompt);

                    String responseText = runLitertLM(prompt);
                    writeFileLog("Model response: " + responseText);

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
                    
                    com.google.gson.JsonArray choices = new com.google.gson.JsonArray();
                    choices.add(choice);
                    responseJson.add("choices", choices);
                    
                    return newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString());
                } catch (Exception e) {
                    writeFileLog("EXCEPTION in serve: " + e.toString());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    writeFileLog(sw.toString());
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        }
        
        private String runLitertLM(String prompt) {
            writeFileLog("runLitertLM called with prompt: " + prompt);
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "litert-lm", "run",
                        "--model-path=" + MODEL_PATH,
                        "--prompt=" + prompt,
                        "--max-tokens=512"
                );
                pb.redirectErrorStream(true);
                writeFileLog("Starting process: " + pb.command());
                
                Process process = pb.start();
                writeFileLog("Process started, waiting for output...");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                writeFileLog("Process output finished, length: " + output.length());
                
                int exitCode = process.waitFor();
                writeFileLog("Process exit code: " + exitCode);
                
                if (exitCode != 0) {
                    return "模型调用失败，退出码: " + exitCode;
                }
                String result = output.toString().trim();
                writeFileLog("Final result: " + result);
                return result;
            } catch (Exception e) {
                writeFileLog("EXCEPTION in runLitertLM: " + e.toString());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                writeFileLog(sw.toString());
                return "模型调用出错: " + e.getMessage();
            }
        }
    }
}
