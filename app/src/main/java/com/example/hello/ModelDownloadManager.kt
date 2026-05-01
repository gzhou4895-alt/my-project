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
        // 使用 Kotlin 的安全调用，防止任何意外崩溃
        kotlin.runCatching {
            val targetDir = context.getExternalFilesDir(null) ?: context.filesDir
            if (!targetDir.exists()) targetDir.mkdirs()
            
            val outputFile = File(targetDir, fileName)
            if (outputFile.exists()) outputFile.delete()

            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 20000
                instanceFollowRedirects = true
            }
            
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("服务器错误: ${connection.responseCode}")
            }

            val fileLength = connection.contentLengthLong
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024 * 16)
            var totalBytesRead = 0L
            var bytesRead: Int

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (fileLength > 0) {
                            val progress = (totalBytesRead * 100 / fileLength).toInt()
                            // 切回主线程安全回调
                            withContext(Dispatchers.Main) { onProgress(progress) }
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) { onProgress(100) }
            connection.disconnect()

        }.onFailure { e ->
            // 如果发生任何异常，捕获它并显示错误，而不是闪退
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "下载引擎崩溃")
            }
        }
    }
}
