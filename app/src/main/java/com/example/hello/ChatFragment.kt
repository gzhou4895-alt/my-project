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

        // 1. 严格对应你 XML 中的 ID
        val logView = view.findViewById<TextView>(R.id.logView)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val etInput = view.findViewById<EditText>(R.id.input) // 对应 android:id="@+id/input"
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        logView.text = "正在初始化 GPU 引擎...\n"

        // 2. 初始化引擎
        GemmaEngine.initialize(requireContext()) { success ->
            activity?.runOnUiThread {
                if (success) {
                    logView.append("✅ GPU 加速就绪 (12GB RAM 已适配)\n")
                } else {
                    logView.append("❌ 模型加载失败，请检查文件路径。\n")
                }
            }
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty() && GemmaEngine.isReady()) {
                // 显示用户消息
                logView.append("\nME: $prompt\n")
                etInput.text.clear()
                
                // 自动滚动到底部
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

                logView.append("\nGEMMA: ")
                btnSend.isEnabled = false

                // 3. 执行推理
                uiExecutor.execute {
                    val response = GemmaEngine.getResponse(prompt)
                    activity?.runOnUiThread {
                        logView.append("$response\n")
                        btnSend.isEnabled = true
                        // 再次滚动到底部
                        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                    }
                }
            }
        }
    }
}
