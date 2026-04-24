package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelsFragment : Fragment() {

    private val modelUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private val modelFileName = "gemma-4-E2B-it.litertlm"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_models, container, false)

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        btnDownload.setOnClickListener {
            val context = requireContext().applicationContext
            
            // UI 状态重置
            btnDownload.isEnabled = false
            btnDownload.text = "连接中..."
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "准备下载..."

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val manager = ModelDownloadManager(context)
                var lastUpdateTime = 0L

                manager.downloadModel(
                    urlString = modelUrl,
                    fileName = modelFileName,
                    onProgress = { progress ->
                        val currentTime = System.currentTimeMillis()
                        // 限制 UI 刷新频率，每 300ms 更新一次，防止主线程卡死崩溃
                        if (currentTime - lastUpdateTime > 300 || progress == 100) {
                            lastUpdateTime = currentTime
                            withContext(Dispatchers.Main) {
                                if (progress == 100) {
                                    progressBar.visibility = View.GONE
                                    tvStatus.text = "下载完成"
                                    btnDownload.isEnabled = true
                                    btnDownload.text = "重新下载"
                                } else if (progress >= 0) {
                                    if (progressBar.isIndeterminate) progressBar.isIndeterminate = false
                                    progressBar.progress = progress
                                    tvStatus.text = "正在下载: $progress%"
                                }
                            }
                        }
                    },
                    onError = { errorMsg ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "错误: $errorMsg", Toast.LENGTH_LONG).show()
                            btnDownload.isEnabled = true
                            btnDownload.text = "点击重试"
                            tvStatus.text = "下载失败"
                            progressBar.visibility = View.GONE
                        }
                    }
                )
            }
        }
        return view
    }
}
