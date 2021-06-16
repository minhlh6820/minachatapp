package com.example.minachatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.log4j.BasicConfigurator;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class ChatMainActivity extends AppCompatActivity {
    private String priKey = null;
    private String username = null;
    private InetSocketAddress socket = null;
    private String[] requestedList = new String[]{};

    private IoSession session = null;
    private NioSocketConnector connector = null;
    private ClientHandler handler = null;
    private ConnectFuture future = null;
//    private Handler activityHandler = new Handler();

    private TextView searchTxt;
    private Button logoutBtn;
    private Button refreshBtn;
    private TextView unameTxt;
    private TextView roomNumTxt;
    private Button addConnectBtn;
    private ListView requestedView;
    private ArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BasicConfigurator.configure();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        priKey = getIntent().getStringExtra("PRI_KEY");
        username = getIntent().getStringExtra("USERNAME");

        this.unameTxt = (TextView) this.findViewById(R.id.unTxtView1);
        this.roomNumTxt = (TextView) this.findViewById(R.id.roomNumTxtView);
        this.requestedView = (ListView) this.findViewById(R.id.requestView);
        this.searchTxt = (TextView) this.findViewById(R.id.editTxtSearchRequest);
        this.logoutBtn = (Button) this.findViewById(R.id.logoutBtn);
        this.addConnectBtn = (Button) this.findViewById(R.id.addConnectBtn);
        this.refreshBtn = (Button) this.findViewById(R.id.refreshBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        unameTxt.setText(username);

        this.openConnector();
        this.initListView(builder);

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ChatMainActivity.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        addConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.stopLogger();
                connector.dispose();

                Intent newIntent = new Intent(ChatMainActivity.this, FriendsListActivity.class);
                newIntent.putExtra("PRI_KEY", priKey);
                newIntent.putExtra("USERNAME", username);
                ChatMainActivity.this.startActivity(newIntent);
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(session.isClosing()) {
                    future = connector.connect(socket);
                    future.awaitUninterruptibly();
                    session = future.getSession();
                }
//                refreshBtn.setEnabled(false);
//                roomNumTxt.setText("Please wait!");
                session.write("GET REQUESTS: " + username);
                session.getCloseFuture().awaitUninterruptibly();
                requestedList = handler.getRequestedList();
                adapter = new ArrayAdapter<String>(ChatMainActivity.this, android.R.layout.simple_list_item_1, requestedList);
                requestedView.setAdapter(adapter);
                roomNumTxt.setText(requestedList.length + " available rooms!");
//                refreshBtn.setEnabled(true);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(session.isClosing()) {
                    future = connector.connect(socket);
                    future.awaitUninterruptibly();
                    session = future.getSession();
                }
                session.write("LOGOUT: " + username);
                session.getCloseFuture().awaitUninterruptibly();
                handler.stopLogger();
                connector.dispose();

                Intent loginIntent = new Intent(ChatMainActivity.this, LoginActivity.class);
                ChatMainActivity.this.startActivity(loginIntent);
            }
        });
    }

    private void openConnector() {
        try {
            MySocket socketObj = new MySocket();
            socket = socketObj.getServerSocket();
            connector = new NioSocketConnector();
            LoggingFilter log = new LoggingFilter();
            ProtocolCodecFilter codec = new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8")));
            connector.getFilterChain().addLast("logger", log);
            connector.getFilterChain().addLast("codec", codec);
            handler = new ClientHandler(username);
            connector.setHandler(handler);
            future = connector.connect(socket);
            future.awaitUninterruptibly();
            session = future.getSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListView(AlertDialog.Builder builder) {
        session.write("GET REQUESTS: " + username);
        session.getCloseFuture().awaitUninterruptibly();
        requestedList = handler.getRequestedList();
        roomNumTxt.setText(requestedList.length + " available rooms!");
//        future = connector.connect(socket);
//        future.awaitUninterruptibly();
//        session = future.getSession();

        requestedView.setEnabled(true);
        requestedView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, requestedList);
        requestedView.setAdapter(adapter);

        requestedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String friendName = (String) requestedView.getItemAtPosition(position);
                if(session.isClosing()) {
                    future = connector.connect(socket);
                    future.awaitUninterruptibly();
                    session = future.getSession();
                }
                session.write("CONNECT: " + username + "--" + friendName);
                session.getCloseFuture().awaitUninterruptibly();
                boolean accepted = handler.isAccepted();

                if(!accepted) {
                    builder.setTitle("Error").setMessage(friendName + "is not connected!");
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                    if(session.isClosing()) {
                        future = connector.connect(socket);
                        future.awaitUninterruptibly();
                        session = future.getSession();
                    }
                    session.write("GET REQUESTS: " + username);
                    session.getCloseFuture().awaitUninterruptibly();
                    requestedList = handler.getRequestedList();
                    adapter = new ArrayAdapter<String>(ChatMainActivity.this, android.R.layout.simple_list_item_1, requestedList);
                    requestedView.setAdapter(adapter);
                    roomNumTxt.setText(requestedList.length + " available rooms!");
                } else {
                    handler.stopLogger();
                    connector.dispose();
                    String port = handler.getPort();
                    String guestIP = handler.getGuestIP();
                    String guestname = handler.getGuestname();
                    String guestPubKey = handler.getGuestPubKey();
//                    boolean server = handler.isServer();

                    Intent chatIntent = new Intent(ChatMainActivity.this, ChatActivity.class);
                    chatIntent.putExtra("PORT", Integer.valueOf(port));
                    chatIntent.putExtra("GUEST_IP", guestIP);
                    chatIntent.putExtra("GUEST_NAME", guestname);
                    chatIntent.putExtra("GUEST_PUB_KEY", guestPubKey);
                    chatIntent.putExtra("PRI_KEY", priKey);
                    chatIntent.putExtra("USERNAME", username);
//                    chatIntent.putExtra("SERVER", server);
                    ChatMainActivity.this.startActivity(chatIntent);
                }
            }
        });
    }
}