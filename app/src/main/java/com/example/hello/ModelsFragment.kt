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

        // 请务必使用 resolve 形式的直链
        val modelUrl = "https://huggingface.co/google/gemma-2b-it/resolve/main/gemma-2b-it.bin?download=true"
        val fileName = "model.bin"

        btnDownload?.setOnClickListener {
            val token = etHfToken?.text?.toString()?.trim()
            
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            tvStatus?.text = "正在连接并验证 Token..."
            
            downloadManager.downloadModel(modelUrl, fileName, token, object : ModelDownloadManager.DownloadCallback {
                override fun onProgress(progress: Int) {
                    progressBar?.progress = progress
                    tvStatus?.text = "下载中: $progress%"
                }

                override fun onSuccess(file: File) {
                    tvStatus?.text = "下载完成: ${file.name}"
                    btnDownload.isEnabled = true
                    Toast.makeText(context, "模型已就绪", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(e: Exception) {
                    tvStatus?.text = "失败: ${e.message}"
                    btnDownload.isEnabled = true
                    progressBar?.visibility = View.GONE
                    Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
