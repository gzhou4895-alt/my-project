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
        if (isEngineInitializing || GemmaEngine.isReady()) return
        
        isEngineInitializing = true
        logView?.append("正在检测本地模型资源...\n")

        try {
            val contextForInit = context?.applicationContext ?: return
            
            GemmaEngine.initialize(contextForInit) { success ->
                activity?.runOnUiThread {
                    if (isAdded) {
                        if (success) {
                            logView?.append("✅ GPU 引擎初始化成功！\n")
                        } else {
                            // --- 【核心修复：动态显示物理路径】 ---
                            val folder = context?.getExternalFilesDir(null)
                            var basePath = folder?.absolutePath ?: "/storage/emulated/0/Android/data/com.example.hello/files"
                            // 强制将显示出来的文字也改为物理路径格式
                            if (basePath.contains("/sdcard/")) {
                                basePath = basePath.replace("/sdcard/", "/storage/emulated/0/")
                            }
                            
                            logView?.append("❌ 引擎启动失败。\n")
                            logView?.append("请确认模型文件是否存在于：\n")
                            logView?.append("$basePath/gemma-4-E2B-it.litertlm\n")
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
        logView = null
    }
}
