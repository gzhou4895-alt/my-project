package com.example.hello

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ModelDownloadManager(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    interface DownloadCallback {
        fun onProgress(progress: Int)
        fun onSuccess(file: File)
        fun onFailure(e: Exception)
    }

    fun downloadModel(modelUrl: String, fileName: String, hfToken: String?, callback: DownloadCallback) {
        val targetFile = File(context.getExternalFilesDir(null), fileName)

        // 文件存在检查
        if (targetFile.exists() && targetFile.length() > 1024 * 1024 * 10) {
            callback.onSuccess(targetFile)
            return
        }

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(modelUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 120000 
                    readTimeout = 120000
                    instanceFollowRedirects = true
                    
                    // 核心：处理 401 授权
                    if (!hfToken.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $hfToken")
                    }
                    
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                    setRequestProperty("Accept-Encoding", "identity")
                    connect()
                }

                if (connection.responseCode == 401) {
                    throw Exception("401 Unauthorized: 需要有效的 HF Token")
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP Error: ${connection.responseCode}")
                }

                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 8)
                var total: Long = 0
                var count: Int
                var lastProgress = 0

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        if (progress != lastProgress) {
                            lastProgress = progress
                            handler.post { callback.onProgress(progress) }
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()
                handler.post { callback.onSuccess(targetFile) }

            } catch (e: Exception) {
                if (targetFile.exists()) targetFile.delete()
                handler.post { callback.onFailure(e) }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
