package com.example.hello

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executors

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var tvStatus: TextView
    private lateinit var rvMessages: RecyclerView
    private val uiExecutor = Executors.newSingleThreadExecutor()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStatus = view.findViewById(R.id.tvChatStatus)
        rvMessages = view.findViewById(R.id.rvMessages)
        val etInput = view.findViewById<EditText>(R.id.etInput)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        // 初始化 RecyclerView
        adapter = ChatAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(context)
        rvMessages.adapter = adapter

        // --- 核心：初始化或获取已有的 GPU 引擎 ---
        tvStatus.text = "正在检测 GPU 资源并加载模型..."
        GemmaEngine.initialize(requireContext()) { success ->
            activity?.runOnUiThread {
                if (success) {
                    tvStatus.text = "✅ GPU 加速已就绪 (12GB RAM 已适配)"
                } else {
                    tvStatus.text = "❌ 模型加载失败，请检查文件是否存在"
                }
            }
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                if (!GemmaEngine.isReady()) {
                    tvStatus.text = "请稍候，引擎正在热身..."
                    return@setOnClickListener
                }

                // 添加用户消息到 UI
                addMessage(prompt, true)
                etInput.text.clear()
                
                tvStatus.text = "Gemma 正在思考 (GPU)..."
                btnSend.isEnabled = false

                // 在后台线程执行推理，防止界面卡死
                uiExecutor.execute {
                    val response = GemmaEngine.getResponse(prompt)
                    activity?.runOnUiThread {
                        addMessage(response, false)
                        tvStatus.text = "✅ GPU 加速已就绪"
                        btnSend.isEnabled = true
                    }
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
    }

    // 注意：如果是作为网关服务器，不要在 Fragment 销毁时 close 引擎
    // 建议在整个 App 退出时才释放资源
}
