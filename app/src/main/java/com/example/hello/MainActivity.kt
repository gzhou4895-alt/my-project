package com.example.hello

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动时检查权限
        checkAndRequestStoragePermission()
    }

    /**
     * 核心权限逻辑：针对 Android 11+ 的特殊处理
     */
    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否已经拥有“所有文件访问权限”
            if (!Environment.isExternalStorageManager()) {
                try {
                    Toast.makeText(this, "请授予所有文件访问权限以加载 AI 模型", Toast.LENGTH_LONG).show()
                    
                    // 跳转到系统的专用授权页面
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, 100)
                } catch (e: Exception) {
                    // 如果上面的跳转失败（极少数定制系统），尝试通用授权页
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, 100)
                }
            }
        } else {
            // Android 10 及以下版本使用传统权限请求（如果需要的话）
            // 这里假设你在清单中声明了并会在 Fragment 中处理，或者在这里调用 requestPermissions
        }
    }

    /**
     * 授权返回后的处理
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "✅ 权限已授予，正在启动引擎", Toast.LENGTH_SHORT).show()
                    // 重新触发一次 Activity 的生命周期，让 Fragment 里的 startGemma() 执行
                    recreate() 
                } else {
                    Toast.makeText(this, "❌ 未获授权，AI 无法读取模型文件", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
