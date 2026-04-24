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

                // ✅ 显式指定 onProgress，避免尾随 lambda 误传给 onError
                manager.downloadModel(
                    urlString = modelUrl,
                    fileName = modelFileName,
                    onProgress = { progress ->
                        launch(Dispatchers.Main) {
                            if (progress == -1) {
                                // 文件大小未知，显示不确定进度
                                progressBar.isIndeterminate = true
                                tvStatus.text = "下载中，请稍候..."
                            } else if (progress in 0..100) {
                                progressBar.isIndeterminate = false
                                progressBar.progress = progress
                                tvStatus.text = "下载中：$progress%"
                            }
                        }
                    },
                    onError = { errorMsg ->
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "下载失败：$errorMsg", Toast.LENGTH_LONG).show()
                            btnDownload.isEnabled = true
                            btnDownload.text = "重新下载"
                            progressBar.visibility = View.GONE
                            tvStatus.text = "下载失败"
                        }
                    }
                )

                // 注意：因为下载完成是在 onProgress(100) 里回调的，
                // 所以“下载完成”的处理放到 onProgress 里判断 progress == 100 更安全。
                // 这里不再需要 withContext 的完成逻辑。
            }
        }

        return view
    }
}
