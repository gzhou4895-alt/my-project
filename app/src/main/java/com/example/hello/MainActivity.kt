package com.example.hello

import android.app.Activity
import android.os.Bundle
import android.widget.*
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var model: LiteModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = LiteModel(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        val input = EditText(this)
        input.hint = "输入数字（测试AI）"

        val button = Button(this)
        button.text = "运行AI"

        val output = TextView(this)
        output.textSize = 18f

        button.setOnClickListener {
            val value = input.text.toString().toFloatOrNull() ?: 0f

            val result = model.run(floatArrayOf(value))

            output.text = "AI结果：${result[0]}"
        }

        layout.addView(input)
        layout.addView(button)
        layout.addView(output)

        setContentView(layout)
    }
}
