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

        // 增强逻辑：如果文件已经存在且超过 1GB，认为已经下载过了
        if (targetFile.exists() && targetFile.length() > 1024 * 1024 * 1024) {
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
                    
                    if (!hfToken.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $hfToken")
                    }
                    
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                    connect()
                }

                if (connection.responseCode == 401) {
                    throw Exception("401: 请检查 Token 是否有权限下载此模型")
                }

                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 16) // 提高到 16KB 缓冲区，下载更快
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
                // 如果下载中断且文件不完整，删除它
                if (targetFile.exists() && targetFile.length() < 1024) {
                    targetFile.delete()
                }
                handler.post { callback.onFailure(e) }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
