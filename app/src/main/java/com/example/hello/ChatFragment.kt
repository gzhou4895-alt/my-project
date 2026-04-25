package com.example.hello

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class ChatFragment : Fragment() {

    private lateinit var viewModel: LogViewModel
    private val runner = LlmRunner()

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

            viewModel.appendLog("👤 用户: $text")

            runModel(text)
        }
    }

    private fun runModel(input: String) {

        runner.run(input) { token ->

            requireActivity().runOnUiThread {
                viewModel.appendLog("🤖 AI: $token")
            }
        }
    }
}
