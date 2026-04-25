package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*

class ChatFragment : Fragment() {

    private lateinit var viewModel: LogViewModel
    private lateinit var input: EditText
    private lateinit var btnSend: Button
    private lateinit var logView: TextView
    private lateinit var scrollView: android.widget.ScrollView  // 添加这行
    private val runner = LlmRunner()
    private var isProcessing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        input = view.findViewById(R.id.input)
        btnSend = view.findViewById(R.id.btnSend)
        logView = view.findViewById(R.id.logView)
        scrollView = view.findViewById(R.id.scrollView)  // 添加这行

        viewModel = ViewModelProvider(this)[LogViewModel::class.java]

        viewModel.logs.observe(viewLifecycleOwner) { logText ->
            logView.text = logText
            // 自动滚动到底部
            scrollView.post {
                scrollView.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnSend.isEnabled = !isLoading
            btnSend.text = if (isLoading) "发送中..." else "发送"
        }

        btnSend.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty() && !isProcessing) {
                sendMessage(text)
            } else if (text.isEmpty()) {
                Toast.makeText(requireContext(), "请输入消息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(message: String) {
        isProcessing = true
        viewModel.setLoading(true)
        input.text.clear()
        viewModel.appendLog("👤 用户: $message")
        viewModel.startAiResponse()
        runModel(message)
    }

    private fun runModel(input: String) {
        runner.run(input) { token ->
            requireActivity().runOnUiThread {
                viewModel.appendAiToken(token)
            }
        }.let { result ->
            requireActivity().runOnUiThread {
                viewModel.finishAiResponse()
                viewModel.setLoading(false)
                isProcessing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runner.cancel()
    }
}
