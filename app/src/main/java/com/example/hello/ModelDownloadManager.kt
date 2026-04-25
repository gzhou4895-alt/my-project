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
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                // ✅ 安全检查 filesDir
                val filesDir = context.filesDir
                    ?: throw IOException("无法访问应用存储目录")
                
                // ✅ 清理文件名，防止路径穿越
                val safeFileName = fileName.replace("/", "_").replace("\\", "_")
                val file = File(filesDir, safeFileName)
                
                // ✅ 确保父目录存在
                file.parentFile?.let { parent ->
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                }

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

                // ✅ 兼容所有版本获取文件大小
                val fileLength = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
                println("fileLength = $fileLength")
                println("下载路径: ${file.absolutePath}")

                // ✅ 检查剩余空间
                if (fileLength > 0L) {
                    val usableSpace = file.usableSpace
                    if (fileLength > usableSpace) {
                        throw IOException(
                            "存储空间不足，需要 ${fileLength / (1024 * 1024)} MB，" +
                            "可用 ${usableSpace / (1024 * 1024)} MB"
                        )
                    }
                }

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
                                // ✅ 在主线程回调
                                withContext(Dispatchers.Main) {
                                    onProgress(progress)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onProgress(-1) // 大小未知
                                }
                            }
                        }
                        outputStream.flush()
                    }
                }

                // 最终进度完成
                withContext(Dispatchers.Main) {
                    onProgress(100)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError?.invoke(e.message ?: "未知网络错误")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
