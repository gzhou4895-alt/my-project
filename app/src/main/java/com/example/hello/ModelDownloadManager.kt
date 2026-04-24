package com.example.hello

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadManager(private val context: Context) {

    // 增加一个可空的错误回调，把失败原因传出去
    fun downloadModel(
        urlString: String,
        fileName: String,
        token: String? = null,   // ⭐ 新增 Token（可选）
        onProgress: (Int) -> Unit,
        onError: ((String) -> Unit)? = null   // ⭐ 新增错误原因回调
    ) {
        var connection: HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null

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

            // 检查 HTTP 响应码
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IOException("Download failed with HTTP $responseCode")
            }

            connection.connect()

            // 支持大于 2GB 的模型文件
            val fileLength = connection.contentLengthLong
            println("fileLength = $fileLength")

            inputStream = connection.inputStream

            val file = File(context.filesDir, fileName)
            outputStream = FileOutputStream(file)

            val buffer = ByteArray(8 * 1024)
            var total: Long = 0
            var count: Int

            while (inputStream!!.read(buffer).also { count = it } != -1) {
                total += count
                outputStream!!.write(buffer, 0, count)

                if (fileLength > 0L) {
                    val progress = ((total * 100) / fileLength).toInt()
                    onProgress(progress)
                } else {
                    onProgress(-1)   // 大小未知，让 UI 显示“下载中…”
                }
            }

            outputStream.flush()
            onProgress(100)

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(-1)                     // 原有错误回调保持
            onError?.invoke(e.message ?: "未知错误")   // 把具体原因传给 UI
        } finally {
            // 保证流被关闭
            try {
                outputStream?.close()
            } catch (_: Exception) {}
            try {
                inputStream?.close()
            } catch (_: Exception) {}
            connection?.disconnect()
        }
    }
}
