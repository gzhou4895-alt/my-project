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
                // 处理 HF 的 S3 跳转
                var finalUrl = modelUrl
                var responseCode: Int
                
                // 最多尝试 5 次重定向
                var redirects = 0
                while (redirects < 5) {
                    val url = URL(finalUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 60000
                        readTimeout = 60000
                        instanceFollowRedirects = false // 手动处理以防丢失 Authorization
                        
                        if (!hfToken.isNullOrBlank()) {
                            setRequestProperty("Authorization", "Bearer $hfToken")
                        }
                        setRequestProperty("User-Agent", "Mozilla/5.0")
                    }
                    
                    responseCode = connection!!.responseCode
                    if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307 || responseCode == 308) {
                        finalUrl = connection!!.getHeaderField("Location")
                        connection!!.disconnect()
                        redirects++
                    } else {
                        break
                    }
                }

                responseCode = connection!!.responseCode
                if (responseCode != 200) {
                    throw Exception("HTTP $responseCode: 路径可能错误或未授权。请确认网页已接受协议。")
                }

                val fileLength = connection!!.contentLength
                val input = BufferedInputStream(connection!!.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 32)
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
