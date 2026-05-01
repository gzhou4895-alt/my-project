package com.example.hello

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.File

class ModelsFragment : Fragment(R.layout.fragment_models) {

    private lateinit var downloadManager: ModelDownloadManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        downloadManager = ModelDownloadManager(requireContext())

        val etHfToken = view.findViewById<EditText>(R.id.etHfToken)
        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        // 尝试使用这个更标准的 Google 官方 LITE RT 路径
        val modelUrl = "https://huggingface.co/google/gemma-2b-it-litert/resolve/main/gemma-2b-it-gpu-int4.bin?download=true" 
        val fileName = "Gemma-4-E2B-it-litert-lm.bin"

        btnDownload?.setOnClickListener {
            val token = etHfToken?.text?.toString()?.trim()
            
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            tvStatus?.text = "正在连接 Hugging Face..."
            
            downloadManager.downloadModel(
                modelUrl, 
                fileName, 
                token, 
                object : ModelDownloadManager.DownloadCallback {
                    override fun onProgress(progress: Int) {
                        progressBar?.progress = progress
                        tvStatus?.text = "下载进度: $progress%"
                    }

                    override fun onSuccess(file: File) {
                        tvStatus?.text = "下载成功！已保存至：${file.name}"
                        btnDownload.isEnabled = true
                        Toast.makeText(context, "模型已就绪", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        // 这里会显示具体的 HTTP 错误码
                        tvStatus?.text = "下载失败详情: ${e.message}"
                        btnDownload.isEnabled = true
                        progressBar?.visibility = View.GONE
                        Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}
