package com.example.hello

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== 根布局 =====
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        // ===== 输入框 =====
        val input = EditText(this)
        input.hint = "输入你的问题..."

        // ===== 按钮 =====
        val button = Button(this)
        button.text = "发送"

        // ===== 输出 =====
        val output = TextView(this)
        output.text = "AI 回复会显示在这里"
        output.textSize = 18f

        // ===== 点击逻辑（不使用模型，保证不崩）=====
        button.setOnClickListener {
            val userText = input.text.toString()

            output.text = """
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
