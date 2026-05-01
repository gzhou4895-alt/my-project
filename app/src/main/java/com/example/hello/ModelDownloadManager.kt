package com.example.hello

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    suspend fun downloadModel(
        urlString: String,
        fileName: String,
        onProgress: suspend (Int) -> Unit,
        onError: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true // 必须允许重定向，HuggingFace 经常跳链接
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onError("服务器拒绝访问: ${connection.responseCode}")
                return@withContext
            }

            val fileLength = connection.contentLengthLong // 使用 Long 防溢出
            inputStream = connection.inputStream
            
            // 使用外部私有空间，安全且空间大
            val targetDir = context.getExternalFilesDir(null) 
                ?: context.filesDir // 后备方案
            val outputFile = File(targetDir, fileName)
            
            // 如果旧文件已存在，先删除，防止写入冲突
            if (outputFile.exists()) {
                outputFile.delete()
            }
            
            outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024 * 16) // 提升至 16KB 缓存，下载更稳
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
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
            onProgress(100)

        } catch (e: SecurityException) {
            e.printStackTrace()
            onError("系统拦截了写入操作，请检查权限配置")
        } catch (e: Exception) {
            e.printStackTrace()
            onError("错误: ${e.localizedMessage ?: "未知网络故障"}")
        } finally {
            // 确保不管成功失败，都关闭流，否则会造成内存泄漏
            try {
                outputStream?.close()
                inputStream?.close()
                connection?.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
