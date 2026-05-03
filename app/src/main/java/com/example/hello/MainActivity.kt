package com.example.hello

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // 1. 设置主布局
            setContentView(R.layout.activity_main)

            // 2. 底部导航点击事件（放在前面，确保点击响应）
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
            bottomNav?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_chat -> {
                        loadFragment(ChatFragment())
                        true
                    }
                    R.id.nav_models -> {
                        loadFragment(ModelsFragment())
                        true
                    }
                    R.id.nav_settings -> {
                        loadFragment(SettingsFragment())
                        true
                    }
                    else -> false
                }
            }

            // 3. 初始进入逻辑：有权限直接进聊天，没权限先引导
            if (hasStoragePermission()) {
                if (savedInstanceState == null) {
                    loadFragment(ChatFragment())
                }
            } else {
                requestStoragePermission()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "启动异常: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 判断权限：Android 11 以上需要管理所有文件权限
     */
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Android 10 以前通常在 Manifest 声明即可，或者请求 READ/WRITE_EXTERNAL_STORAGE
            true 
        }
    }

    /**
     * 请求跳转至系统授权界面
     */
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
            Toast.makeText(this, "请在设置中开启“所有文件访问权限”以加载 AI 模型", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 当用户从设置界面授予权限回来后，自动加载聊天界面
        if (hasStoragePermission()) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment == null) {
                loadFragment(ChatFragment())
            }
        }
    }

    /**
     * 通用的 Fragment 切换方法
     */
    private fun loadFragment(fragment: Fragment) {
        try {
            // 使用 commit() 替代 commitAllowingStateLoss() 除非遇到特定的生命周期问题
            // replace 确保容器内的内容被替换，避免叠加
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
