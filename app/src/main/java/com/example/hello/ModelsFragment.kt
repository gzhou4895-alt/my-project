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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelsFragment : Fragment() {

    private val modelUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    private val modelFileName = "gemma-4-E2B-it.litertlm"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_models, container, false)

        val btnDownload = view.findViewById<Button>(R.id.btnDownload)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvDownloadStatus)

        btnDownload.setOnClickListener {
            Toast.makeText(requireContext(), "按钮被点击了！", Toast.LENGTH_LONG).show()
            btnDownload.text = "下载中..."
            progressBar.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "准备下载..."

            CoroutineScope(Dispatchers.IO).launch {
                val manager = ModelDownloadManager(requireContext())
                manager.downloadModel(modelUrl, modelFileName)
            }
        }

        return view
    }
}
