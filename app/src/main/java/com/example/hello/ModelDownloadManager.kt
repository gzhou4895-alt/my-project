package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ModelsFragment : Fragment() {

    private lateinit var downloadManager: ModelDownloadManager
    private val modelUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private val modelFileName = "gemma-4-E2B-it.litertlm"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_models, container, false)
        downloadManager = ModelDownloadManager(requireContext())

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        val existingPath = downloadManager.getModelPath(modelFileName)
        if (existingPath != null) {
            btnDownload.text = "已下载"
            btnDownload.isEnabled = false
            tvStatus.text = "模型已就绪: $modelFileName"
            tvStatus.visibility = View.VISIBLE
        }

        btnDownload.setOnClickListener {
            lifecycleScope.launch {
                btnDownload.isEnabled = false
                btnDownload.text = "下载中..."
                progressBar.visibility = View.VISIBLE
                tvStatus.visibility = View.VISIBLE

                downloadManager.downloadModel(modelUrl, modelFileName)

                downloadManager.state.collect { state ->
                    when (state) {
                        is DownloadState.Downloading -> {
                            progressBar.progress = (state.progress * 100).toInt()
                            tvStatus.text = "下载中: ${(state.progress * 100).toInt()}%"
                        }
                        is DownloadState.Success -> {
                            tvStatus.text = "下载完成！"
                            btnDownload.text = "已完成"
                            progressBar.visibility = View.GONE
                        }
                        is DownloadState.Error -> {
                            tvStatus.text = "下载失败: ${state.message}"
                            btnDownload.text = "重试"
                            btnDownload.isEnabled = true
                            progressBar.visibility = View.GONE
                        }
                        is DownloadState.Idle -> {}
                    }
                }
            }
        }

        return view
    }
}
