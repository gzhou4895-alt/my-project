package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // 首次打开默认显示聊天页
        if (savedInstanceState == null) {
            loadFragment(ChatFragment())
        }

        // 点击底部按钮切换页面
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> loadFragment(ChatFragment())
                R.id.nav_models -> loadFragment(ModelsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}            output.text = """
                🤖 模拟AI回复：
                
                你刚刚输入的是：
                $userText
                
                （当前未加载模型）
            """.trimIndent()
        }

        // ===== 加入布局 =====
        layout.addView(input)
        layout.addView(button)
        layout.addView(output)

        setContentView(layout)
    }
}
