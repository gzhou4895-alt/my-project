package com.example.hello;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 80, 60, 80);

        TextView title = new TextView(this);
        title.setText("LiteRT Adapter");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView spacer1 = new TextView(this);
        spacer1.setText(" ");
        spacer1.setTextSize(20);
        layout.addView(spacer1);

        Button startBtn = new Button(this);
        startBtn.setText("启动适配器服务");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, LiteRTService.class));
            }
        });
        layout.addView(startBtn);

        TextView spacer2 = new TextView(this);
        spacer2.setText(" ");
        spacer2.setTextSize(16);
        layout.addView(spacer2);

        Button stopBtn = new Button(this);
        stopBtn.setText("停止服务");
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, LiteRTService.class));
            }
        });
        layout.addView(stopBtn);

        setContentView(layout);
    }
}
