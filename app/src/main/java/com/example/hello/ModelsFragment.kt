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

    private val modelUrl =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
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

            Toast.makeText(context, "开始下载模型", Toast.LENGTH_SHORT).show()

            btnDownload.isEnabled = false
            btnDownload.text = "下载中..."
            progressBar.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "准备下载..."

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                val manager = ModelDownloadManager(context)

                // ✅ 显式指定 onProgress 和 onError
                manager.downloadModel(
                    urlString = modelUrl,
                    fileName = modelFileName,
                    onProgress = { progress ->
                        // 用 withContext 回主线程更新 UI（launch 在这里不可用）
                        withContext(Dispatchers.Main) {
                            when {
                                progress == -1 -> {
                                    // 大小未知，显示转圈
                                    progressBar.isIndeterminate = true
                                    tvStatus.text = "下载中，请稍候..."
                                }
                                progress == 100 -> {
                                    // 下载完成
                                    progressBar.isIndeterminate = false
                                    progressBar.progress = 100
                                    btnDownload.isEnabled = true
                                    btnDownload.text = "重新下载"
                                    progressBar.visibility = View.GONE
                                    tvStatus.text = "下载完成"
                                }
                                progress in 0..99 -> {
                                    progressBar.isIndeterminate = false
                                    progressBar.progress = progress
                                    tvStatus.text = "下载中：$progress%"
                                }
                            }
                        }
                    },
                    onError = { errorMsg ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "下载失败：$errorMsg", Toast.LENGTH_LONG).show()
                            btnDownload.isEnabled = true
                            btnDownload.text = "重新下载"
                            progressBar.visibility = View.GONE
                            tvStatus.text = "下载失败"
                        }
                    }
                )
            }
        }

        return view
    }
}
