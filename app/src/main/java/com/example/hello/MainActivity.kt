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
            // 1. 设置布局
            setContentView(R.layout.activity_main)

            // 2. 检查权限：如果没权限，先跳转去申请，不加载聊天界面
            if (!hasStoragePermission()) {
                requestStoragePermission()
            } else {
                if (savedInstanceState == null) {
                    loadFragment(ChatFragment())
                }
            }

            // 3. 底部导航逻辑
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
            bottomNav?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_chat -> loadFragment(ChatFragment())
                    R.id.nav_models -> loadFragment(ModelsFragment())
                    R.id.nav_settings -> loadFragment(SettingsFragment())
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "启动异常，请检查布局文件", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

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
            Toast.makeText(this, "请开启权限以允许加载模型", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从设置页面回来后，如果拿到权限了就加载界面
        if (hasStoragePermission()) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment == null) {
                loadFragment(ChatFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
