package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ChatFragment : Fragment() {

    private var logView: TextView? = null
    private var etInput: EditText? = null
    private var btnSend: Button? = null
    private var scrollView: ScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        
        logView = view.findViewById(R.id.log_view)
        etInput = view.findViewById(R.id.et_input)
        btnSend = view.findViewById(R.id.btn_send)
        scrollView = view.findViewById(R.id.scroll_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化引擎
        logView?.text = "正在初始化 AI 引擎...\n"
        context?.let { ctx ->
            GemmaEngine.initialize(ctx) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        logView?.append("✅ 引擎就绪，开始聊天吧！\n")
                    } else {
                        logView?.append("❌ 引擎加载失败，请检查模型文件或显存。\n")
                    }
                }
            }
        }

        // 2. 【核心修复】设置流式监听器
        // 借鉴官方：AI 每吐出一个 token，这里就会被回调一次
        GemmaEngine.setOnResultListener { partialText, isDone ->
            activity?.runOnUiThread {
                // 将新出的字追加到 logView
                logView?.append(partialText)
                
                if (isDone) {
                    // 回答结束，恢复按钮，换行
                    btnSend?.isEnabled = true
                    logView?.append("\n------------------\n")
                }
                
                // 实时滚动到底部
                scrollToBottom()
            }
        }

        // 3. 发送按钮点击事件
        btnSend?.setOnClickListener {
            val prompt = etInput?.text.toString().trim()
            if (prompt.isNotEmpty() && GemmaEngine.isReady()) {
                // 显示用户输入
                logView?.append("\n我: $prompt\nAI: ")
                etInput?.text?.clear()
                
                // 禁用按钮防止连续点击导致引擎崩溃
                btnSend?.isEnabled = false
                
                // 【核心修复】调用异步发送接口，不再使用 getResponse
                GemmaEngine.sendPrompt(prompt)
                
                scrollToBottom()
            }
        }
    }

    private fun scrollToBottom() {
        scrollView?.post {
            scrollView?.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 页面销毁时可以考虑是否关闭引擎，通常建议在 App 退出时才 close
    }
}
