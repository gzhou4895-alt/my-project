package com.example.hello

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val uiExecutor = Executors.newSingleThreadExecutor()
    private var isEngineInitializing = false // 防止重复初始化

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logView = view.findViewById<TextView>(R.id.logView)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val etInput = view.findViewById<EditText>(R.id.input)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        logView.text = "等待权限授予...\n"

        // 核心：只有拿到权限才初始化，否则直接加载会闪退
        if (hasStoragePermission()) {
            startGemma()
        } else {
            logView.append("⚠️ 请在主界面授予文件权限后重试。\n")
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
            } else if (!GemmaEngine.isReady()) {
                Toast.makeText(context, "引擎尚未就绪，请检查模型文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else true
    }

    private fun startGemma() {
        if (isEngineInitializing) return
        isEngineInitializing = true
        
        val logView = view?.findViewById<TextView>(R.id.logView)
        logView?.append("正在检测本地模型资源...\n")

        // 使用 try-catch 彻底包裹初始化过程，防止引擎底层代码在读取 2.6G 模型时崩溃
        try {
            GemmaEngine.initialize(requireContext().applicationContext) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        logView?.append("✅ GPU 引擎初始化成功！\n")
                    } else {
                        logView?.append("❌ 引擎启动失败。请确认模型文件路径及文件名正确。\n")
                    }
                    isEngineInitializing = false
                }
            }
        } catch (e: Exception) {
            logView?.append("💥 初始化发生严重错误: ${e.message}\n")
            isEngineInitializing = false
        }
    }

    // 当用户授予权限回来时，MainActivity 会触发 onResume，我们可以在这里检测并启动
    override fun onResume() {
        super.onResume()
        if (hasStoragePermission() && !GemmaEngine.isReady() && !isEngineInitializing) {
            startGemma()
        }
    }
}
