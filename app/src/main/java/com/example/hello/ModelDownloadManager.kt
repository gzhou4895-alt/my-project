package com.example.hello

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    suspend fun downloadModel(
        urlString: String,
        fileName: String,
        onProgress: suspend (Int) -> Unit,
        onError: suspend (String) -> Unit
    ) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onError("服务器返回错误: ${connection.responseCode}")
                return
            }

            val fileLength = connection.contentLength
            val inputFile = connection.inputStream
            // 下载到应用私有目录，安全且无需动态权限
            val outputFile = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(8192) // 8KB 缓冲区
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputFile.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (fileLength > 0) {
                    val progress = (totalBytesRead * 100 / fileLength).toInt()
                    onProgress(progress)
                } else {
                    onProgress(-1) // 长度未知
                }
            }

            outputStream.flush()
            outputStream.close()
            inputFile.close()
            onProgress(100)

        } catch (e: Exception) {
            onError(e.localizedMessage ?: "未知网络错误")
        }
    }
}

