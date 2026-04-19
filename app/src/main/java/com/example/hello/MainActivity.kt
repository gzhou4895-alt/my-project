package com.example.hello

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 80, 60, 80)
        }
        
        val title = TextView(this).apply {
            text = "LiteRT Adapter"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        layout.addView(title)
        
        val spacer = TextView(this).apply {
            text = " "
            textSize = 20f
        }
        layout.addView(spacer)
        
        val startBtn = Button(this).apply {
            text = "启动服务"
            setOnClickListener {
                startService(Intent(this@MainActivity, LiteRTService::class.java))
                Toast.makeText(this@MainActivity, "✅ 服务已启动，端口 8080", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(startBtn)
        
        setContentView(layout)
    }
}
