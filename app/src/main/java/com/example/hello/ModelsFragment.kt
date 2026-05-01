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

        // 【最精准 URL 修复】
        val modelUrl = "https://huggingface.co/google/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true" 
        val fileName = "gemma-4-E2B-it.litertlm"

        btnDownload?.setOnClickListener {
            val token = etHfToken?.text?.toString()?.trim()
            
            progressBar?.visibility = View.VISIBLE
            tvStatus?.visibility = View.VISIBLE
            btnDownload.isEnabled = false
            tvStatus?.text = "连接中 (正在定位官方 LFS 文件)..."
            
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
                        tvStatus?.text = "下载成功！文件已存入私有目录"
                        btnDownload.isEnabled = true
                        Toast.makeText(context, "模型已就绪", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(e: Exception) {
                        // 显示完整错误信息以便调试
                        tvStatus?.text = "错误详情: ${e.message}"
                        btnDownload.isEnabled = true
                        // 404 往往是路径问题，403 是没点协议，401 是 Token 错
                        Toast.makeText(context, "操作失败，请看提示", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}
