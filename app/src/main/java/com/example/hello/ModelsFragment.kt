package com.example.hello

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ModelsFragment : Fragment(R.layout.fragment_models) {

    private lateinit var downloadManager: ModelDownloadManager

    override fun onViewCreated(view: View) {
        super.onViewCreated(view: View)

        downloadManager = ModelDownloadManager(requireContext())

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        // 这里的链接请替换为你真正的 Hugging Face 直链
        val modelUrl = "https://huggingface.co/your-user/your-model/resolve/main/model.bin?download=true"
        val fileName = "model.bin"

        btnDownload.setOnClickListener {
            btnDownload.isEnabled = false
            tvStatus.text = "Connecting to server..."
            
            downloadManager.downloadModel(modelUrl, fileName, object : ModelDownloadManager.DownloadCallback {
                override fun onProgress(progress: Int) {
                    // 更新进度条
                    progressBar.progress = progress
                    tvStatus.text = "Downloading: $progress%"
                }

                override fun onSuccess(file: File) {
                    tvStatus.text = "Success: ${file.name}"
                    btnDownload.isEnabled = true
                    Toast.makeText(context, "Model ready!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(e: Exception) {
                    tvStatus.text = "Error: ${e.message}"
                    btnDownload.isEnabled = true
                    Toast.makeText(context, "Download Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
