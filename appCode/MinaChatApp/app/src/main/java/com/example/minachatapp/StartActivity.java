package com.example.minachatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button loginBtn;
    private Button registerBtn;
    private String serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        this.loginBtn = (Button) this.findViewById(R.id.loginBtn);
        this.registerBtn = (Button) this.findViewById(R.id.registerBtn);

        this.serverIp = getIntent().getStringExtra("SERVERIP");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                loginIntent.putExtra("SERVERIP", serverIp);
                StartActivity.this.startActivity(loginIntent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}