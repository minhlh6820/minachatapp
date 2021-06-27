package com.example.minachatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView errorStartTxtView;
    private EditText ipEditTxt;
    private Button nextStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.errorStartTxtView = (TextView) this.findViewById(R.id.errorStartTxtView);
        this.ipEditTxt = (EditText) this.findViewById(R.id.ipEditTxt);
        this.nextStartBtn = (Button) this.findViewById(R.id.nextStartBtn);

        nextStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = false;
                String serverIp = ipEditTxt.getText().toString().trim();
                if(serverIp != "") {
                    Pattern pattern = Pattern.compile("([0-9a-f]{0,4}:){5,7}([0-9a-f]){1,4}", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(serverIp);
                    if(matcher.find())
                        check = true;
                }
                if(!check) {
                    errorStartTxtView.setText("Invalid IPv6 address");
                } else {
                    Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
                    startIntent.putExtra("SERVERIP", serverIp);
                    MainActivity.this.startActivity(startIntent);
                }
            }
        });
    }
}