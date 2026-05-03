package com.example.hello

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val uiExecutor = Executors.newSingleThreadExecutor()
    private var isEngineInitializing = false 

    // 使用 lazy 绑定，避免在异步回调中 findViewById 找不到 view
    private var logView: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logView = view.findViewById(R.id.logView)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val etInput = view.findViewById<EditText>(R.id.input)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        logView?.text = "等待权限授予...\n"

        if (hasStoragePermission()) {
            startGemma()
        } else {
            logView?.append("⚠️ 请在主界面授予文件权限以激活 AI。\n")
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                if (GemmaEngine.isReady()) {
                    logView?.append("\nME: $prompt\n")
                    etInput.text.clear()
                    scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

                    logView?.append("\nGEMMA: ")
                    btnSend.isEnabled = false

                    uiExecutor.execute {
                        val response = GemmaEngine.getResponse(prompt)
                        activity?.runOnUiThread {
                            logView?.append("$response\n")
                            btnSend.isEnabled = true
                            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                        }
                    }
                } else {
                    Toast.makeText(context, "引擎尚未就绪，请稍后...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else true
    }

    private fun startGemma() {
        // 增加防御：如果已经就绪或正在初始化，直接返回
        if (isEngineInitializing || GemmaEngine.isReady()) return
        
        isEngineInitializing = true
        logView?.append("正在检测本地模型资源...\n")

        try {
            // 安全获取 Context
            val contextForInit = context?.applicationContext ?: return
            
            GemmaEngine.initialize(contextForInit) { success ->
                // 安全回到主线程更新 UI
                activity?.runOnUiThread {
                    if (isAdded) { // 确保 Fragment 还在
                        if (success) {
                            logView?.append("✅ GPU 引擎初始化成功！\n")
                        } else {
                            logView?.append("❌ 引擎启动失败。请确认模型文件路径：\n/sdcard/Android/data/com.example.hello/files/gemma-4-E2B-it.litertlm\n")
                        }
                    }
                    isEngineInitializing = false
                }
            }
        } catch (e: Exception) {
            logView?.append("💥 初始化异常: ${e.message}\n")
            isEngineInitializing = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasStoragePermission() && !GemmaEngine.isReady()) {
            startGemma()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logView = null // 释放引用，防止内存泄漏
    }
}
