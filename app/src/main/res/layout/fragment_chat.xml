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

    // 定义控件变量
    private var logView: TextView? = null
    private var etInput: EditText? = null
    private var btnSend: Button? = null
    private var scrollView: ScrollView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局文件
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        
        // --- 严格对齐你提供的 XML 中的 ID ---
        logView = view.findViewById(R.id.logView)
        etInput = view.findViewById(R.id.input)
        btnSend = view.findViewById(R.id.btnSend)
        scrollView = view.findViewById(R.id.scrollView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 AI 引擎
        logView?.text = "正在准备 AI 助手...\n"
        context?.let { ctx ->
            GemmaEngine.initialize(ctx) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        logView?.append("✅ 引擎已就绪，请输入问题。\n")
                    } else {
                        logView?.append("❌ 加载失败，请检查模型文件路径或显存。\n")
                    }
                }
            }
        }

        // 2. 设置监听器，实现流式输出（打字机效果）
        GemmaEngine.setOnResultListener { partialText, isDone ->
            activity?.runOnUiThread {
                // 追加显示的文字
                logView?.append(partialText)
                
                if (isDone) {
                    btnSend?.isEnabled = true
                    logView?.append("\n------------------\n")
                }
                // 自动滚动
                scrollToBottom()
            }
        }

        // 3. 发送按钮逻辑
        btnSend?.setOnClickListener {
            val prompt = etInput?.text.toString().trim()
            if (prompt.isNotEmpty() && GemmaEngine.isReady()) {
                logView?.append("\n我: $prompt\nAI: ")
                etInput?.text?.clear()
                
                // 禁用按钮防止乱点
                btnSend?.isEnabled = false
                
                // 异步发送指令给 Gemma
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
}
