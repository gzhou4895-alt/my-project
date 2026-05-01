package com.example.hello

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val uiExecutor = Executors.newSingleThreadExecutor()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logView = view.findViewById<TextView>(R.id.logView)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val etInput = view.findViewById<EditText>(R.id.input)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        logView.text = "正在检测本地模型资源...\n"

        GemmaEngine.initialize(requireContext()) { success ->
            activity?.runOnUiThread {
                if (success) {
                    logView.append("✅ GPU 引擎初始化成功！\n")
                } else {
                    logView.append("❌ 引擎启动失败。请确认模型文件在 /Android/data/com.example.hello/files/ 目录下，且文件名为 gemma-4-E2B-it.litertlm\n")
                }
            }
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty() && GemmaEngine.isReady()) {
                logView.append("\nME: $prompt\n")
                etInput.text.clear()
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

                logView.append("\nGEMMA: ")
                btnSend.isEnabled = false

                uiExecutor.execute {
                    val response = GemmaEngine.getResponse(prompt)
                    activity?.runOnUiThread {
                        logView.append("$response\n")
                        btnSend.isEnabled = true
                        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                    }
                }
            }
        }
    }
}
