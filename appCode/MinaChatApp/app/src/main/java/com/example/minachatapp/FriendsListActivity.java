package com.example.minachatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

public class FriendsListActivity extends AppCompatActivity {
    private String priKey = null;
    private String username = null;
    private InetSocketAddress socket = null;
    private String[] connectedList = new String[]{};

    private IoSession session = null;
    private NioSocketConnector connector = null;
    private ClientHandler handler = null;
    private ConnectFuture future = null;

    private TextView searchTxt;
    private Button backBtn;
    private ListView connectedListView;
    private ArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BasicConfigurator.configure();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        priKey = getIntent().getStringExtra("PRI_KEY");
        username = getIntent().getStringExtra("USERNAME");

        this.connectedListView = (ListView) this.findViewById(R.id.connectedListView);
        this.searchTxt = (TextView) this.findViewById(R.id.editTextSearchFriend);
        this.backBtn = (Button) this.findViewById(R.id.btnBackChatMain);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        this.openConnector();
        this.initListView(builder);

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FriendsListActivity.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.stopLogger();
                connector.dispose();

                Intent newIntent = new Intent(FriendsListActivity.this, ChatMainActivity.class);
                newIntent.putExtra("PRI_KEY", priKey);
                newIntent.putExtra("USERNAME", username);
                FriendsListActivity.this.startActivity(newIntent);
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
        session.write("GET FRIENDS: " + username);
        session.getCloseFuture().awaitUninterruptibly();
        connectedList = handler.getFriendsList();
//        future = connector.connect(socket);
//        future.awaitUninterruptibly();
//        session = future.getSession();

        connectedListView.setEnabled(true);
        connectedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, connectedList);
        connectedListView.setAdapter(adapter);

        connectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String friendName = (String) connectedListView.getItemAtPosition(position);
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
                } else {
                    handler.stopLogger();
                    connector.dispose();
                    String port = handler.getPort();
                    String guestIP = handler.getGuestIP();
                    String guestname = handler.getGuestname();
                    String guestPubKey = handler.getGuestPubKey();
//                    boolean server = handler.isServer();

                    Intent chatIntent = new Intent(FriendsListActivity.this, ChatActivity.class);
                    chatIntent.putExtra("PORT", Integer.valueOf(port));
                    chatIntent.putExtra("GUEST_IP", guestIP);
                    chatIntent.putExtra("GUEST_NAME", guestname);
                    chatIntent.putExtra("GUEST_PUB_KEY", guestPubKey);
                    chatIntent.putExtra("PRI_KEY", priKey);
                    chatIntent.putExtra("USERNAME", username);
//                    chatIntent.putExtra("SERVER", server);
                    FriendsListActivity.this.startActivity(chatIntent);
                }
            }
        });
    }
}