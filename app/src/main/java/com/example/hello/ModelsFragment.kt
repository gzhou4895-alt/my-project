package com.example.hello

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
        return try {
            val view = inflater.inflate(R.layout.fragment_models, container, false)
            downloadManager = ModelDownloadManager(requireContext())

            val btnDownload = view.findViewById<Button>(R.id.btnDownload)
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

            btnDownload.setOnClickListener {
                Toast.makeText(requireContext(), "开始下载", Toast.LENGTH_SHORT).show()
                btnDownload.text = "下载中..."
                btnDownload.isEnabled = false
                progressBar.visibility = View.VISIBLE
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "正在连接..."

                lifecycleScope.launch {
                    try {
                        downloadManager.downloadModel(modelUrl, modelFileName)
                        downloadManager.state.collect { state ->
                            when (state) {
                                is DownloadState.Downloading -> {
                                    val percent = (state.progress * 100).toInt()
                                    progressBar.progress = percent
                                    tvStatus.text = "下载中: ${percent}%"
                                }
                                is DownloadState.Success -> {
                                    tvStatus.text = "下载完成！"
                                    btnDownload.text = "已完成"
                                    progressBar.visibility = View.GONE
                                }
                                is DownloadState.Error -> {
                                    tvStatus.text = "下载失败"
                                    btnDownload.text = "重试"
                                    btnDownload.isEnabled = true
                                    progressBar.visibility = View.GONE
                                }
                                is DownloadState.Idle -> {}
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ModelsFragment", "下载异常", e)
                        Toast.makeText(requireContext(), "错误: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            view
        } catch (e: Exception) {
            Log.e("ModelsFragment", "创建视图异常", e)
            throw e
        }
    }
}
