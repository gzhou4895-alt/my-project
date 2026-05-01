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

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                var currentUrl = modelUrl
                var responseCode: Int
                
                // 循环处理重定向，防止 S3 节点跳转丢失 Token
                var redirectCount = 0
                while (true) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 120000
                        readTimeout = 120000
                        instanceFollowRedirects = false // 手动处理防止 Header 丢失
                        
                        if (!hfToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $hfToken")
                        }
                        setRequestProperty("User-Agent", "Mozilla/5.0")
                    }

                    responseCode = connection.responseCode
                    // 如果是 301 或 302 重定向
                    if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307 || responseCode == 308) {
                        currentUrl = connection.getHeaderField("Location")
                        connection.disconnect()
                        redirectCount++
                        if (redirectCount > 5) throw Exception("重定向次数过多")
                        continue
                    }
                    break
                }

                if (responseCode == 401) throw Exception("401 Unauthorized: Token 无效")
                if (responseCode == 403) throw Exception("403 Forbidden: 请在 HF 网页点击 Accept License 接受协议")
                if (responseCode != 200) throw Exception("HTTP 错误码: $responseCode")

                val fileLength = connection!!.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 16)
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
