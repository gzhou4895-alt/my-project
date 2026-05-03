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
import java.io.File
import java.util.concurrent.Executors

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val uiExecutor = Executors.newSingleThreadExecutor()
    private var isEngineInitializing = false 
    private var logView: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logView = view.findViewById(R.id.logView)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val etInput = view.findViewById<EditText>(R.id.input)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        // 初始状态保持简洁
        if (!hasStoragePermission()) {
            logView?.text = "System: 等待权限授予...\n"
        } else {
            logView?.text = "System: 正在准备 AI 引擎...\n"
            startGemma()
        }

        btnSend.setOnClickListener {
            val prompt = etInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                if (GemmaEngine.isReady()) {
                    // 1. 显示用户输入 (模仿图 1 简洁感)
                    logView?.append("\nYOU: $prompt\n")
                    etInput.text.clear()
                    
                    // 自动滚动
                    scrollToBottom(scrollView)

                    // 2. 准备显示 AI 回复
                    logView?.append("\nGEMMA: ")
                    btnSend.isEnabled = false

                    uiExecutor.execute {
                        val response = GemmaEngine.getResponse(prompt)
                        activity?.runOnUiThread {
                            // 3. 实时追加回复内容
                            logView?.append("$response\n")
                            btnSend.isEnabled = true
                            scrollToBottom(scrollView)
                        }
                    }
                } else {
                    Toast.makeText(context, "引擎尚未就绪，请稍后...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startGemma() {
        if (isEngineInitializing || GemmaEngine.isReady()) {
            if (GemmaEngine.isReady()) logView?.text = "" // 已就绪则保持清爽
            return
        }
        
        isEngineInitializing = true

        try {
            val contextForInit = context?.applicationContext ?: return
            
            GemmaEngine.initialize(contextForInit) { success ->
                activity?.runOnUiThread {
                    if (isAdded) {
                        if (success) {
                            // --- 【关键改动：成功后清空所有报错日志】 ---
                            logView?.text = "" 
                            Toast.makeText(context, "AI 引擎已就绪", Toast.LENGTH_SHORT).show()
                        } else {
                            // 失败时才显示调试信息
                            val folder = context?.getExternalFilesDir(null)
                            val basePath = folder?.absolutePath ?: "/storage/emulated/0/Android/data/com.example.hello/files"
                            
                            logView?.text = "❌ 引擎启动失败\n"
                            logView?.append("请确保模型文件已放置在：\n")
                            logView?.append("📂 $basePath/gemma-4-E2B-it.litertlm")
                        }
                    }
                    isEngineInitializing = false
                }
            }
        } catch (e: Exception) {
            logView?.append("\n💥 运行异常: ${e.message}\n")
            isEngineInitializing = false
        }
    }

    private fun scrollToBottom(scrollView: ScrollView) {
        scrollView.post { 
            scrollView.fullScroll(View.FOCUS_DOWN) 
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else true
    }

    override fun onResume() {
        super.onResume()
        if (hasStoragePermission() && !GemmaEngine.isReady() && !isEngineInitializing) {
            startGemma()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logView = null
    }
}
