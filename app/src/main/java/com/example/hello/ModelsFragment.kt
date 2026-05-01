package com.example.hello

import android.os.Bundle
import android.view.View
import android.widget.Button
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

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        // 核心修复：对应你的 XML 里的 ID: tvDownloadStatus
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        // 替换为你真实的 Hugging Face 下载直链
        val modelUrl = "https://huggingface.co/your-user/your-model/resolve/main/model.bin?download=true"
        val fileName = "model.bin"

        btnDownload?.setOnClickListener {
            // 点击后显示进度条和状态
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            
            tvStatus?.text = "Connecting..."
            
            downloadManager.downloadModel(modelUrl, fileName, object : ModelDownloadManager.DownloadCallback {
                override fun onProgress(progress: Int) {
                    progressBar?.progress = progress
                    tvStatus?.text = "Downloading: $progress%"
                }

                override fun onSuccess(file: File) {
                    tvStatus?.text = "Success: ${file.name}"
                    btnDownload.isEnabled = true
                    Toast.makeText(context, "Model ready!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(e: Exception) {
                    tvStatus?.text = "Error: ${e.message}"
                    btnDownload.isEnabled = true
                    // 失败时隐藏进度条，方便用户重试
                    progressBar?.visibility = View.GONE
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
