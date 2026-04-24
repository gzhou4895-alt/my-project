package com.example.hello

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    data class Success(val filePath: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ModelDownloadManager(private val context: Context) {

    private val client = OkHttpClient()
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state

    suspend fun downloadModel(url: String, fileName: String) {
        _state.value = DownloadState.Downloading(0f)

        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("下载失败，HTTP状态码: ${response.code}")
                }

                val body = response.body ?: throw Exception("响应体为空")
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()

                val modelDir = File(context.filesDir, "models")
                if (!modelDir.exists()) modelDir.mkdirs()
                val modelFile = File(modelDir, fileName)

                val outputStream = FileOutputStream(modelFile)
                val buffer = ByteArray(8192)
                var downloadedBytes = 0L
                var bytes: Int

                while (inputStream.read(buffer).also { bytes = it } != -1) {
                    outputStream.write(buffer, 0, bytes)
                    downloadedBytes += bytes

                    if (contentLength > 0) {
                        val progress = downloadedBytes.toFloat() / contentLength
                        _state.value = DownloadState.Downloading(progress)
                    }
                }

                outputStream.close()
                inputStream.close()

                _state.value = DownloadState.Success(modelFile.absolutePath)
            }
        } catch (e: Exception) {
            _state.value = DownloadState.Error(e.message ?: "下载过程中出现未知错误")
        }
    }

    fun getModelPath(fileName: String): String? {
        val modelFile = File(context.filesDir, "models/$fileName")
        return if (modelFile.exists()) modelFile.absolutePath else null
    }
}
