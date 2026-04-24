package com.example.hello

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    /**
     * 下载模型文件
     * @param onProgress 挂起回调，用于更新进度
     * @param onError 挂起回调，用于处理错误
     */
    suspend fun downloadModel(
        urlString: String,
        fileName: String,
        token: String? = null,
        onProgress: suspend (Int) -> Unit,
        onError: (suspend (String) -> Unit)? = null
    ) {
        // 将整个下载逻辑限制在 IO 调度器中执行
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"

                // 设置 Header
                token?.let {
                    connection.setRequestProperty("Authorization", "Bearer $it")
                }
                connection.setRequestProperty("Accept", "*/*")

                // 检查响应码
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IOException("下载失败，HTTP 状态码: $responseCode")
                }

                val fileLength = connection.contentLengthLong
                val file = File(context.filesDir, fileName)

                // 使用 use 函数自动关闭流，防止内存泄漏
                connection.inputStream.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(8 * 1024)
                        var total: Long = 0
                        var count: Int

                        while (inputStream.read(buffer).also { count = it } != -1) {
                            total += count
                            outputStream.write(buffer, 0, count)

                            // 计算进度并回调
                            if (fileLength > 0L) {
                                val progress = ((total * 100) / fileLength).toInt()
                                onProgress(progress)
                            } else {
                                onProgress(-1) // 大小未知
                            }
                        }
                        outputStream.flush()
                    }
                }

                // 最终进度完成
                onProgress(100)

            } catch (e: Exception) {
                e.printStackTrace()
                // 调用错误回调
                onError?.invoke(e.message ?: "未知网络错误")
            } finally {
                connection?.disconnect()
            }
        }
    }
}

