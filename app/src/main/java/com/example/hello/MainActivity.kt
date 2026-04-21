package com.example.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "App Running OK ✅"
        tv.textSize = 22f

        setContentView(tv)
    }
}
