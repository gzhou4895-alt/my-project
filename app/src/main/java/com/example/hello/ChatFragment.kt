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

        adapter = ChatAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(context)
        rvMessages.adapter = adapter

        tvStatus.text = "正在初始化 GPU 引擎..."
        GemmaEngine.initialize(requireContext()) { success ->
            activity?.runOnUiThread {
                tvStatus.text = if (success) "✅ GPU 加速就绪" else "❌ 模型未就绪"
            }
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty() && GemmaEngine.isReady()) {
                addMessage(prompt, true)
                etInput.text.clear()
                tvStatus.text = "Gemma 正在思考..."
                
                uiExecutor.execute {
                    val response = GemmaEngine.getResponse(prompt)
                    activity?.runOnUiThread {
                        addMessage(response, false)
                        tvStatus.text = "✅ GPU 加速就绪"
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
}
