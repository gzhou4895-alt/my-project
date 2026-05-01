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

        // 【核心修改：文件保护逻辑】
        // 如果文件存在且大于 1GB，说明模型已经搬运成功，直接回调成功，不准往下走
        if (targetFile.exists() && targetFile.length() > 1024 * 1024 * 1024) {
            handler.post { callback.onSuccess(targetFile) }
            return
        }

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(modelUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    if (!hfToken.isNullOrBlank()) {
                        setRequestProperty("Authorization", "Bearer $hfToken")
                    }
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }

                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    throw Exception("HTTP $responseCode: 无法连接服务器")
                }

                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(targetFile)

                val data = ByteArray(1024 * 32)
                var total: Long = 0
                var count: Int
                
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        handler.post { callback.onProgress(progress) }
                    }
                }

                output.flush()
                output.close()
                input.close()
                handler.post { callback.onSuccess(targetFile) }

            } catch (e: Exception) {
                // 【核心修改：只在文件极小时（下载失败的残片）才删除】
                if (targetFile.exists() && targetFile.length() < 1024 * 1024) {
                    targetFile.delete()
                }
                handler.post { callback.onFailure(e) }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
