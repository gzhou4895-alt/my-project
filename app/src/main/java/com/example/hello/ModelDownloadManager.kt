package com.example.hello

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    fun downloadModel(
        urlString: String,
        fileName: String,
        onProgress: (Int) -> Unit
    ) {
        var connection: HttpURLConnection? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection

            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.connect()

            val fileLength = connection.contentLength
            println("fileLength = $fileLength") // 👉 调试用

            val inputStream = connection.inputStream

            // 👉 存储路径（App私有目录）
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8 * 1024)
            var total: Long = 0
            var count: Int

            while (inputStream.read(buffer).also { count = it } != -1) {
                total += count
                outputStream.write(buffer, 0, count)

                // 👉 进度计算
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    onProgress(progress)
                } else {
                    onProgress(-2) // 未知进度
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            onProgress(100)

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(-1) // 下载失败
        } finally {
            connection?.disconnect()
        }
    }
}
