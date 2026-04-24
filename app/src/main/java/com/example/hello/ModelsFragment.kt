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

            Toast.makeText(context, "开始下载模型", Toast.LENGTH_SHORT).show()

            btnDownload.text = "下载中..."
            progressBar.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "准备下载..."

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                val manager = ModelDownloadManager(context)

                manager.downloadModel(
                    modelUrl,
                    modelFileName
                ) { progress ->

                    // 👉 回到主线程更新 UI
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

                        if (progress == -1) {
                            tvStatus.text = "下载失败 ❌"
                            btnDownload.text = "重试"
                            return@launch
                        }

                        tvStatus.text = "下载进度: $progress%"
                        progressBar.progress = progress
                    }
                }

                // 👉 下载完成
                withContext(Dispatchers.Main) {
                    tvStatus.text = "下载完成 ✅"
                    btnDownload.text = "重新下载"
                }
            }
        }

        return view
    }
}
