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

    fun downloadModel(modelUrl: String, fileName: String, callback: DownloadCallback) {
        // 使用 getExternalFilesDir，这样不需要额外申请存储权限，Android 14 也稳
        val targetFile = File(context.getExternalFilesDir(null), fileName)

        // 如果文件已经存在，直接返回成功
        if (targetFile.exists() && targetFile.length() > 1024 * 1024) {
            callback.onSuccess(targetFile)
            return
        }

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                Log.d("Download", "Starting download from: $modelUrl")
                val url = URL(modelUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    // 马来西亚环境加固：给足握手时间
                    connectTimeout = 120000 
                    readTimeout = 120000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    setRequestProperty("Accept-Encoding", "identity")
                    connect()
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP ${connection.responseCode}")
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

                    // 计算进度
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
                Log.e("Download", "Download failed: ${e.message}")
                // 失败了就把残缺的文件删掉，防止下次进来以为下好了
                if (targetFile.exists()) targetFile.delete()
                handler.post { callback.onFailure(e) }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
