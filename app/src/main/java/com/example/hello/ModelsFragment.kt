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

        val modelUrl = "https://huggingface.co/google/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true"
        val fileName = "gemma-4-E2B-it.litertlm"

        btnDownload?.setOnClickListener {
            val token = etHfToken?.text?.toString()?.trim()
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            tvStatus?.text = "正在检查本地资源..."
            
            downloadManager.downloadModel(modelUrl, fileName, token, object : ModelDownloadManager.DownloadCallback {
                override fun onProgress(progress: Int) {
                    progressBar?.progress = progress
                    tvStatus?.text = "进度: $progress%"
                }

                override fun onSuccess(file: File) {
                    tvStatus?.text = "✅ 模型已就绪"
                    btnDownload.isEnabled = true
                    Toast.makeText(context, "加载成功！", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(e: Exception) {
                    tvStatus?.text = "提示: ${e.message}"
                    btnDownload.isEnabled = true
                }
            })
        }
    }
}
