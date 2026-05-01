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
            // 路径逻辑：即便没内存卡，也强制使用手机自带 512G 的安全目录
            val targetDir = context.getExternalFilesDir(null) ?: context.filesDir
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            
            val outputFile = File(targetDir, fileName)
            // 如果存在旧文件先删除，防止“文件已存在”导致的无法写入闪退
            if (outputFile.exists()) { outputFile.delete() }

            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 20000
            connection.readTimeout = 20000
            connection.instanceFollowRedirects = true // 允许重定向
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onError("服务器拒绝(${connection.responseCode})")
                return@withContext
            }

            val fileLength = connection.contentLengthLong
            inputStream = connection.inputStream
            outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024 * 16) // 16KB 缓冲区
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

        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.localizedMessage ?: "未知网络错误")
        } finally {
            // 彻底关闭所有连接，防止内存溢出
            try {
                outputStream?.close()
                inputStream?.close()
                connection?.disconnect()
            } catch (e: Exception) {}
        }
    }
}
