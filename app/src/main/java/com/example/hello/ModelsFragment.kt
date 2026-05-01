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

        // 修改点：对齐你给出的文件名
        val modelUrl = "https://huggingface.co/google/gemma-2b-it-litert/resolve/main/2b-it-gpu-int4.bin?download=true" 
        val fileName = "Gemma-4-E2B-it-litert-lm.bin" // 加上 .bin 后缀方便系统识别

        btnDownload?.setOnClickListener {
            val token = etHfToken?.text?.toString()?.trim()
            
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            tvStatus?.text = "正在连接服务器..."
            
            downloadManager.downloadModel(
                modelUrl, 
                fileName, 
                token, 
                object : ModelDownloadManager.DownloadCallback {
                    override fun onProgress(progress: Int) {
                        progressBar?.progress = progress
                        tvStatus?.text = "正在下载: $progress%"
                    }

                    override fun onSuccess(file: File) {
                        tvStatus?.text = "下载成功！文件存放在私有目录"
                        btnDownload.isEnabled = true
                        Toast.makeText(context, "模型 ${file.name} 已就绪", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        tvStatus?.text = "下载失败: ${e.message}"
                        btnDownload.isEnabled = true
                        progressBar?.visibility = View.GONE
                        Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}
