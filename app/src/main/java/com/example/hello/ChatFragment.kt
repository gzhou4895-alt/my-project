package com.example.hello

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ChatFragment : Fragment() {

    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<EditText>(R.id.input)
        val btn = view.findViewById<Button>(R.id.btnSend)
        val logView = view.findViewById<TextView>(R.id.logView)

        viewModel = ViewModelProvider(this)[LogViewModel::class.java]

        // 👉 实时刷新 UI
        viewModel.logs.observe(viewLifecycleOwner) {
            logView.text = it
        }

        btn.setOnClickListener {
            val text = input.text.toString()

            viewModel.appendLog("输入: $text")

            // 👉 这里先模拟 LiteRT
            runFakeModel(text)
        }
    }

    private fun runFakeModel(input: String) {
        Thread {
            viewModel.appendLog("模型开始加载...")

            var result = ""

            for (c in input) {
                Thread.sleep(100) // 模拟流式输出
                result += c
                viewModel.appendLog("token: $result")
            }

            viewModel.appendLog("推理完成")
        }.start()
    }
}
