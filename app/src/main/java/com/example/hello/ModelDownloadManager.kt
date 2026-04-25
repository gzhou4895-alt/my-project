package com.example.hello

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    /**
     * @param onProgress 回调进度 (0-100)，-1 表示长度未知
     * @param onError 错误回调
     */
    suspend fun downloadModel(
        urlString: String,
        fileName: String,
        onProgress: suspend (Int) -> Unit,
        onError: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) { // 强制在 IO 线程运行，防止阻塞主线程
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000 // 15秒超时
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onError("服务器响应错误: ${connection.responseCode}")
                return@withContext
            }

            val fileLength = connection.contentLength
            val inputFile = connection.inputStream
            
            // 下载到内部存储 filesDir，无需申请读写权限，最安全
            val outputFile = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024 * 8) // 8KB 缓冲区
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputFile.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (fileLength > 0) {
                    val progress = (totalBytesRead * 100 / fileLength).toInt()
                    onProgress(progress)
                } else {
                    onProgress(-1)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputFile.close()
            onProgress(100) // 确保最后返回 100

        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.localizedMessage ?: "网络连接异常")
        }
    }
}
