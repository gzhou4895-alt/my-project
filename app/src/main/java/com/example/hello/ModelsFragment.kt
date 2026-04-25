package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelsFragment : Fragment() {

    private val modelUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private val modelFileName = "gemma-4-E2B-it.litertlm"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_models, container, false)

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        btnDownload.setOnClickListener {
            val context = requireContext().applicationContext
            
            // 1. 初始化 UI 状态
            btnDownload.isEnabled = false
            btnDownload.text = "正在连接..."
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true 
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "准备下载模型..."
            Toast.makeText(context, "开始下载，请勿关闭插件", Toast.LENGTH_SHORT).show()

            // 2. 启动协程下载
            viewLifecycleOwner.lifecycleScope.launch {
                val manager = ModelDownloadManager(context)
                var lastUiUpdateTime = 0L // 用于限制 UI 刷新频率

                manager.downloadModel(
                    urlString = modelUrl,
                    fileName = modelFileName,
                    onProgress = { progress ->
                        val currentTime = System.currentTimeMillis()
                        // 节流逻辑：每 300ms 更新一次 UI，或下载完成时更新
                        if (currentTime - lastUiUpdateTime > 300 || progress == 100) {
                            lastUiUpdateTime = currentTime
                            withContext(Dispatchers.Main) {
                                if (progress == 100) {
                                    progressBar.visibility = View.GONE
                                    tvStatus.text = "模型下载成功！"
                                    btnDownload.isEnabled = true
                                    btnDownload.text = "重新下载"
                                } else if (progress >= 0) {
                                    progressBar.isIndeterminate = false
                                    progressBar.progress = progress
                                    tvStatus.text = "下载进度: $progress%"
                                }
                            }
                        }
                    },
                    onError = { errorMsg ->
                        withContext(Dispatchers.Main) {
                            tvStatus.text = "下载失败: $errorMsg"
                            btnDownload.isEnabled = true
                            btnDownload.text = "重试下载"
                            progressBar.visibility = View.GONE
                            Toast.makeText(context, "下载出错，请检查网络", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
        return view
    }
}
