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
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // --- 核心修复：权限检查逻辑 ---
        if (!hasStoragePermission()) {
            // 如果没权限，先不加载 Fragment，直接去要权限
            requestStoragePermission()
        } else {
            // 有权限，正常加载界面
            if (savedInstanceState == null) {
                loadFragment(ChatFragment())
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> loadFragment(ChatFragment())
                R.id.nav_models -> loadFragment(ModelsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }

    /**
     * 检查是否拥有 Android 11+ 的所有文件管理权限
     */
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Android 10 以下通常在安装时或通过普通权限申请
            true
        }
    }

    /**
     * 跳转到系统设置页面让用户手动开启权限
     */
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 100)
            }
            Toast.makeText(this, "请开启权限以允许加载模型文件", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 当用户从设置界面授予权限回来后，自动加载界面
     */
    override fun onResume() {
        super.onResume()
        if (hasStoragePermission()) {
            // 检查当前是否已经加载了 Fragment，防止重复加载
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment == null) {
                loadFragment(ChatFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // 增加一个简单的 try-catch，防止 Fragment 内部初始化崩溃影响主程序
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "切换界面时发生错误", Toast.LENGTH_SHORT).show()
        }
    }
}
