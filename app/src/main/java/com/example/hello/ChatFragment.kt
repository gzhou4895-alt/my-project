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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*

class ChatFragment : Fragment() {

    private lateinit var viewModel: LogViewModel
    private lateinit var input: EditText
    private lateinit var btnSend: Button
    private lateinit var logView: TextView
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

        viewModel = ViewModelProvider(this)[LogViewModel::class.java]

        viewModel.logs.observe(viewLifecycleOwner) { logText ->
            logView.text = logText
            val scrollView = view.findViewById<android.widget.ScrollView>(R.id.scrollView)
            scrollView?.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
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

class LogViewModel : ViewModel() {
    
    private val _logs = MutableLiveData<String>("")
    val logs: LiveData<String> = _logs
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val currentResponse = StringBuilder()
    private var isInAiResponse = false
    
    fun appendLog(message: String) {
        val currentLog = _logs.value ?: ""
        _logs.value = if (currentLog.isEmpty()) message else "$currentLog\n$message"
    }
    
    fun startAiResponse() {
        currentResponse.clear()
        isInAiResponse = true
        appendLog("🤖 AI: ")
    }
    
    fun appendAiToken(token: String) {
        if (!isInAiResponse) {
            startAiResponse()
        }
        currentResponse.append(token)
        
        val currentLog = _logs.value ?: ""
        val lines = currentLog.split("\n")
        
        if (lines.isNotEmpty() && lines.last().startsWith("🤖 AI: ")) {
            val newLastLine = "🤖 AI: $currentResponse"
            val newLog = lines.dropLast(1).joinToString("\n") + 
                        (if (lines.size > 1) "\n" else "") + newLastLine
            _logs.value = newLog
        } else {
            _logs.value = "$currentLog🤖 AI: $currentResponse"
        }
    }
    
    fun finishAiResponse() {
        isInAiResponse = false
        currentResponse.clear()
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun clearLogs() {
        _logs.value = ""
        finishAiResponse()
    }
}

class LlmRunner {
    
    private var currentJob: Job? = null
    
    fun run(input: String, onToken: (String) -> Unit): Job {
        cancel()
        
        currentJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = simulateApiCall(input)
                for (char in response) {
                    delay(50)
                    withContext(Dispatchers.Main) {
                        onToken(char.toString())
                    }
                }
            } catch (e: CancellationException) {
                // 取消任务
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onToken("\n[错误: ${e.message}]")
                }
            }
        }
        
        return currentJob!!
    }
    
    private suspend fun simulateApiCall(input: String): String {
        delay(1000)
        return "这是对 \"$input\" 的回复。这是一个流式输出的示例。"
    }
    
    fun cancel() {
        currentJob?.cancel()
        currentJob = null
    }
}
