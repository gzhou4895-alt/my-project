package com.example.hello;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
        startBtn.setText("启动服务");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, LiteRTService.class));
                Toast.makeText(MainActivity.this, "✅ 服务已启动，端口 8080", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(startBtn);

        setContentView(layout);
    }
} com.example.hello;

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

        Button startBtn = new Button(this);
        startBtn.setText("启动服务");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, LiteRTService.class));
            }
        });
        layout.addView(startBtn);

        setContentView(layout);
    }
}
