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
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.connect()

            val fileLength = connection.contentLength

            if (fileLength <= 0) {
                onProgress(0)
            }

            val inputStream = connection.inputStream

            // 👉 存储路径（App私有目录）
            val file = File(context.filesDir, fileName)

            val outputStream = FileOutputStream(file)

            val data = ByteArray(8 * 1024)
            var total: Long = 0
            var count: Int

            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                outputStream.write(data, 0, count)

                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    onProgress(progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            onProgress(100)

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(-1) // 👉 失败标志
        }
    }
}
