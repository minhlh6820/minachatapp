package com.example.minachatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.BasicConfigurator;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.KeyPair;

public class LoginActivity extends AppCompatActivity {
    private Button backBtn;
    private Button submitLoginBtn;
    private EditText usernameTxt;
    private EditText pwdTxt;

    private KeyPair keyPair = null;
    private String pubKey;
    private String priKey;
    private String serverIp;

    private Key keyObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BasicConfigurator.configure();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        keyObj = new Key();

        this.backBtn = (Button) this.findViewById(R.id.btnBack);
        this.submitLoginBtn = (Button) this.findViewById(R.id.btnSubmitLogin);
        this.usernameTxt = (EditText) this.findViewById(R.id.editTextUsername);
        this.pwdTxt = (EditText) this.findViewById(R.id.editTextPwd);

        this.serverIp = getIntent().getStringExtra("SERVERIP");

        submitLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean accepted = false;
                String username = usernameTxt.getText().toString().trim();
                String pwd = pwdTxt.getText().toString().trim();
                if (username == "" || pwd == "") {
                    Toast.makeText(LoginActivity.this, "Error! Please enter username and password!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        keyPair = keyObj.generateKeyPair();
                        pubKey = keyObj.getPubKeyStr(keyPair);
                        String msg = "LOGIN: " + username + "--" + pwd + "--" + pubKey;
                        MySocket socketObj = new MySocket(serverIp);
                        InetSocketAddress socket = socketObj.getServerSocket();

                        NioSocketConnector connector = new NioSocketConnector();
                        LoggingFilter log = new LoggingFilter();
                        ProtocolCodecFilter codec = new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8")));
                        connector.getFilterChain().addLast("logger", log);
                        connector.getFilterChain().addLast("codec", codec);
                        ClientHandler handler = new ClientHandler(username);
                        connector.setHandler(handler);
                        ConnectFuture future = connector.connect(socket);
                        future.awaitUninterruptibly();
                        IoSession session = future.getSession();
                        session.write(msg);
                        session.getCloseFuture().awaitUninterruptibly();
                        accepted = handler.isAccepted();
                        handler.stopLogger();
                        connector.dispose();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
//                System.out.println("Accepted: " + accepted);
                if(accepted == true) {
                    Intent submitIntent = new Intent(LoginActivity.this, ChatMainActivity.class);
                    priKey = keyObj.getPriKeyStr(keyPair);
                    submitIntent.putExtra("PRI_KEY", priKey);
                    submitIntent.putExtra("USERNAME", username);
                    submitIntent.putExtra("SERVERIP", serverIp);
                    LoginActivity.this.startActivity(submitIntent);
                } else {
                    Toast.makeText(LoginActivity.this, "Error! Wrong account!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });
    }
}