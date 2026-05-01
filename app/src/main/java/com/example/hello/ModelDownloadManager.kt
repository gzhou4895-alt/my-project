package com.example.hello

import android.content.Context
import android.os.Handler
import android.os.Looper
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

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                // 处理 Hugging Face 常见的重定向 (Redirect)
                var downloadUrl = modelUrl
                var responseCode: Int
                
                // 允许最多 3 次跳转 (针对 LFS 存储节点)
                repeat(3) {
                    val url = URL(downloadUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 60000
                        readTimeout = 60000
                        instanceFollowRedirects = true
                        if (!hfToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $hfToken")
                        }
                        setRequestProperty("User-Agent", "Mozilla/5.0")
                    }
                    responseCode = connection!!.responseCode
                    if (responseCode == 301 || responseCode == 302) {
                        downloadUrl = connection!!.getHeaderField("Location")
                        connection!!.disconnect()
                    }
                }

                responseCode = connection!!.responseCode
                if (responseCode != 200) {
                    throw Exception("HTTP $responseCode: 无法获取文件，请确认 Token 和协议签署")
                }

                val fileLength = connection!!.contentLength
                val input = BufferedInputStream(connection!!.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 32) // 32KB 缓冲区，针对大马宽带优化
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
