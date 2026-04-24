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
        token: String? = null,   // ⭐ 新增 Token（可选）
        onProgress: (Int) -> Unit
    ) {
        var connection: HttpURLConnection? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection

            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"

            // ⭐ 如果有 token 就加 header
            token?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }

            connection.setRequestProperty("Accept", "*/*")

            connection.connect()

            val fileLength = connection.contentLength
            println("fileLength = $fileLength")

            val inputStream = connection.inputStream

            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8 * 1024)
            var total: Long = 0
            var count: Int

            while (inputStream.read(buffer).also { count = it } != -1) {
                total += count
                outputStream.write(buffer, 0, count)

                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    onProgress(progress)
                } else {
                    onProgress(-2)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            onProgress(100)

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(-1)
        } finally {
            connection?.disconnect()
        }
    }
}
