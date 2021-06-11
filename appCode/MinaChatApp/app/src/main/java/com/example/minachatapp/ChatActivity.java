package com.example.minachatapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String username = null;
    private PrivateKey priKey = null;
    private String guestIp = null;
    private String guestname = null;
    private PublicKey guestPubKey = null;
    private int port = -1;
//    private boolean server = false;

    private ArrayList<String> chatViewList = new ArrayList<String>();
    private ArrayList<Message> receivedMsgList = new ArrayList<Message>();
    private ArrayList<Message> sendMsgList = new ArrayList<Message>();
    private ArrayList<Message> removeMsgList = new ArrayList<Message>();
    private Map<String, String[]> fileDataMap = new HashMap<String, String[]>();
    private int tagNumber = 1;
//    private String finalMsg = "NULL";

    private BufferedReader in = null;
    private BufferedWriter out = null;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private Handler handler = new Handler();
    private Key keyObj = new Key();
//    private MySocket socketObj;

    private Button sendBtn = null;
    private TextView guestNameTxt = null;
    private TextView errorChatTxt = null;
    private TextView chatTxt = null;
    private ListView chatView = null;
    private Button exitChatBtn = null;
    private ArrayAdapter<String> adapter = null;
    private FileChooserFragment fragment;
    private Button uploadBtn;

    private boolean isExit = false;
//    private boolean disconnected = true;

    private void log(String msg, boolean isUser, boolean disconnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
//                String viewTxt = name + " --- " + msg;
                if(!isUser) {
                    String viewTxt = guestname + "--" + msg;
                    int index = -1;
                    int tagNum = Integer.valueOf(msg.split("//")[0].trim());

                    if(chatViewList.size() != 0) {
                        Iterator i = chatViewList.iterator();
                        while(i.hasNext()) {
                            String tmp = (String) i.next();
                            if(tmp.contains(guestname)) {
                                String msgTmp = tmp.split("--")[1].trim();
                                int tagNumTmp = Integer.valueOf(msgTmp.split("//")[0].trim());
                                if(tagNum < tagNumTmp) {
                                    index = chatViewList.indexOf(tmp);
                                    break;
                                }
                            }
                        }
                    }

                    if(index != -1) chatViewList.add(index, viewTxt);
                    else chatViewList.add(viewTxt);
//                    chatViewList.add(viewTxt);
//                    chatViewList.add(chatView);

                } else {
                    String viewTxt = username + "--" + msg;
                    if(disconnected) {
                        viewTxt += "--" + "Sending";
                        chatViewList.add(viewTxt);
//                        chatView.setStatus(false);
//                        chatViewList.add(chatView);
                    } else {
                        viewTxt += "--" + "Sent";
                        int index = -1;
                        String tmp = "";
                        if(chatViewList.size() != 0) {
//                            ArrayList<String> replaceList = new ArrayList<String>();
//                            Iterator i = msgList.iterator();
//                            while(i.hasNext()) {
//                                String txt = (String) i.next();
//                                if(txt.contains("Not send")) {
//                                    i.remove();
//                                    txt = txt.replaceAll("Not send", "Send");
//                                    replaceList.add(txt);
//                                }
//                            }
//                            msgList.addAll(replaceList);


//                            Iterator i = chatViewList.iterator();
//                            while(i.hasNext()) {
//                                ChatView obj = (ChatView) i.next();
//                                if(obj.getContent().equals(chatView.getContent()) && obj.getTagNumber() == chatView.getTagNumber() && obj.getStatus() == false) {
//                                    int index = chatViewList.indexOf(obj);
////                                    i.remove();
//                                    obj.setStatus(true);
//                                    chatViewList.set(index, obj);
//                                    isExist = true;
//                                    break;
//                                }
//                            }

//                            System.out.println(msg);

                            Iterator i = chatViewList.iterator();
                            while(i.hasNext()) {
                                String tmpStr = (String) i.next();
                                String[] strList = tmpStr.split("--");

                                if(strList[0].equals(username) && strList[1].equals(msg)) {
                                    index = chatViewList.indexOf(tmpStr);
//                                    i.remove();
                                    if(strList[2].equals("Sending"))
                                        tmp = tmpStr.replaceAll("Sending", "Sent");
                                }
                            }
                        }
                        if(index == -1) chatViewList.add(viewTxt);
                        else if(tmp != "") chatViewList.set(index, tmp);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

//    private boolean searchServer() {
//        try {
//            socketObj = new MySocket(guestIp, port);
//            InetSocketAddress socketAddress = socketObj.getServerSocket();
//            socket = new Socket();
//            socket.connect(socketAddress);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
////            System.out.println("Find server");
//            return true;
//        } catch (Exception e) {
////            System.out.println("Create server");
////            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private void createServer() {
//        try {
//            serverSocket = new ServerSocket(port);
//            socket = serverSocket.accept();
//            InetSocketAddress remoteSock = (InetSocketAddress) socket.getRemoteSocketAddress();
//            if(remoteSock != null) {
//                String remoteaddr = remoteSock.getAddress().toString();
//                if(remoteaddr.contains("%")) {
//                    remoteaddr = remoteaddr.split("%")[0].trim();
//                }
//                remoteaddr = remoteaddr.replaceAll("/", "");
//                if(!remoteaddr.equals(guestIp)) {
//                    socket.close();
//                    System.out.println("Incorrect guest IP");
//                }
//            }
//        } catch (Exception e) {
//
//        }
//    }

    private void startUDPSender(String msg) {
        try {
            MySocket socketObj = new MySocket(guestIp, port);
            InetSocketAddress socketAddress = socketObj.getServerSocket();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    byte[] data = msg.getBytes();
                    try {
                        DatagramSocket das = new DatagramSocket();
                        das.setBroadcast(true);
                        DatagramPacket packet = new DatagramPacket(data, data.length, socketAddress);
                        das.send(packet);
//                        System.out.println("Send message");
                        das.close();
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }
            });
            t.start();
        } catch (UnknownHostException ue) {
            errorChatTxt.setText("Disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAutoSender() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isExit) {
                    try {
                        if(removeMsgList.size() != 0) {
                            sendMsgList.removeAll(removeMsgList);
                            removeMsgList.clear();
                        }
//                        System.out.println(sendMsgList.size());
                        if(sendMsgList.size() != 0) {
                            Iterator i = sendMsgList.iterator();
                            while(i.hasNext()) {
                                Message msgObj = (Message) i.next();
                                String securedMsg = msgObj.createSecuredMsg();
                                startUDPSender(securedMsg);
                            }
                        }
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private boolean containNull(String[] strList) {
        for(int i=0; i<strList.length; i++) {
            if(strList[i] == null)
                return true;
        }
        return false;
    }

    private void startUDPServer() {
        Thread t = new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
//                    MySocket socketObj = new MySocket(guestIp, port);
//                    InetSocketAddress socketAddress = socketObj.getServerSocket();
                    DatagramSocket ds = new DatagramSocket(port);
//                    ds.connect(socketAddress);

                    byte[] data = new byte[65535];
                    String tmp;
                    while(!isExit) {
//                        System.out.println("Received message!");
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        ds.receive(packet);
                        tmp = new String(data).trim();
                        if(tmp != "") {
                            Message obj = new Message(tmp, guestname, priKey, guestPubKey, 0);
                            obj.readSecuredMsg();
                            String content = obj.getContent();
                            if (content == null) {
                                errorChatTxt.setText("Error message!");
                            } else {
//                                System.out.println("Received message: " + receivedMsg);
                                if(content.equals("EXIT")) {
                                    receivedMsgList.clear();
                                    tagNumber = 0;
                                }

                                if(!content.contains("RECEIVED")) {
                                    boolean isReceived = false;
                                    int tagNum = obj.getTagNumber();
                                    Iterator i = receivedMsgList.iterator();
                                    while(i.hasNext()) {
                                        Message msgObjTmp = (Message) i.next();
                                        if(msgObjTmp.getTagNumber() == tagNum && msgObjTmp.getContent().equals(content)) {
                                            isReceived = true;
                                            break;
                                        }
                                    }

                                    if(!isReceived) {
                                        if(content.contains("FILE")) {
                                            String fileMsg = content.replaceAll("FILE ", "");
                                            String[] infoFile = fileMsg.split("::");
                                            String fileName = infoFile[0].trim();
                                            int index = Integer.valueOf(infoFile[1].trim());
                                            int numMsg = Integer.valueOf(infoFile[2].trim());
                                            String tmpData = infoFile[3];

//                                            System.out.println(fileMsg);

                                            if(fileDataMap.containsKey(fileName)) {
                                                String[] fileContent = fileDataMap.get(fileName);
                                                fileContent[index] = tmpData;

                                                if (!containNull(fileContent)) {
                                                    log(tagNum + "//" + "File " + fileName, false, false);
                                                    String completedMsg = "RECEIVED FILE " + fileName;
                                                    Message completedMsgObj = new Message(completedMsg, username, priKey, guestPubKey, 0);
                                                    completedMsg = completedMsgObj.createSecuredMsg();
                                                    startUDPSender(completedMsg);
                                                }
                                            } else {
                                                String[] fileContent = new String[numMsg+1];
                                                fileContent[index] = tmpData;
                                                fileDataMap.put(fileName, fileContent);
                                            }

//                                        ChatView chatViewObj = new ChatView(-1, guestname, "File" + fileName);
//                                        chatViewObj.setStatus(true);
//                                        log(chatViewObj, false, false);
                                        } else {
//                                        ChatView chatViewObj = new ChatView(-1, guestname, content);
//                                        chatViewObj.setStatus(true);
//                                        log(chatViewObj, false, false);
                                            log(tagNum + "//" + content, false, false);
                                        }
                                        receivedMsgList.add(obj);
                                    }

                                    String responseMsg = "RECEIVED MESSAGE " + tagNum;
                                    Message resMsgObj = new Message(responseMsg, username, priKey, guestPubKey, 0);
                                    String resMsg = resMsgObj.createSecuredMsg();
                                    startUDPSender(resMsg);
                                }

                                if(content.contains("RECEIVED")) {
                                    if(content.contains("RECEIVED FILE")) {
                                        String fileName = content.replaceAll("RECEIVED FILE ", "");
                                        if(fileDataMap.containsKey(fileName))
                                            fileDataMap.remove(fileName);
                                        log("File " + fileName, true, false);
//                                        ChatView chatViewObj = new ChatView(tagNum, username, "File " + fileName);
//                                        chatViewObj.setStatus(true);
//                                        log(chatViewObj, true, false)
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorChatTxt.setText("Uploading completed");
                                                uploadBtn.setEnabled(true);
                                            }
                                        });
                                    } else {
                                        int tagNum = Integer.valueOf(content.replaceAll("RECEIVED MESSAGE ", ""));

                                        Iterator i = sendMsgList.iterator();
                                        while(i.hasNext()) {
                                            Message msgObjTmp = (Message) i.next();
                                            if(msgObjTmp.getTagNumber() == tagNum) {
                                                removeMsgList.add(msgObjTmp);
                                            }
                                        }

                                        if(!removeMsgList.isEmpty()) {
                                            i = removeMsgList.iterator();
                                            while(i.hasNext()) {
                                                Message objTmp = (Message) i.next();
                                                content = objTmp.getOriginalContent();
                                                if(!content.contains("FILE")) {
                                                    log(tagNum + "//" + content, true, false);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        data = new byte[65535];
                    }
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fileChooserFragment);

//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        this.username = getIntent().getStringExtra("USERNAME");
        String priKeyStr = getIntent().getStringExtra("PRI_KEY");
        this.port = getIntent().getIntExtra("PORT", -1);
        this.guestIp = getIntent().getStringExtra("GUEST_IP");
        this.guestname = getIntent().getStringExtra("GUEST_NAME");
        String guestPubKeyStr = getIntent().getStringExtra("GUEST_PUB_KEY");
//        this.server = getIntent().getBooleanExtra("SERVER", true);

        try {
            priKey = keyObj.getPriKeyFromStr(priKeyStr);
            guestPubKey = keyObj.getPubKeyFromStr(guestPubKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.guestNameTxt = (TextView) this.findViewById(R.id.guestNameTxt);
        this.chatTxt = (TextView) this.findViewById(R.id.chatEditTxt);
        this.errorChatTxt = (TextView) this.findViewById(R.id.errorChatTxtView);
        this.sendBtn = (Button) this.findViewById(R.id.sendBtn);
        this.exitChatBtn = (Button) this.findViewById(R.id.btnChatExit);
        this.uploadBtn = (Button) this.findViewById(R.id.uploadFileBtn);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        System.out.println(guestname + "-" + guestIp + "-" + port);

//        guestNameTxt.setText(guestname + ": Disconnected");
        guestNameTxt.setText(guestname);

        this.chatView = (ListView) this.findViewById(R.id.chatView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatViewList);
        chatView.setAdapter(adapter);
//        chatView.setAdapter(new ChatViewAdapter(this, chatViewList));

//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while (!isExit) {
//                        if (!searchServer()) {
//                            createServer();
//                        }
//
//                        if (socket.isConnected()) {
//                            disconnected = false;
//                            tagNumber = 0;
//                            receivedMsgList.clear();
//
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    guestNameTxt.setText(guestname + ": Connected!");
////                                    sendBtn.setEnabled(true);
//                                }
//                            });
//
//                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//                            while (!isExit && !disconnected) {
//                                String line = in.readLine();
//                                if (line != null) {
//                                    Message msgObj = new Message(line, guestname, priKey, guestPubKey);
//                                    String receivedMsg = msgObj.readSecuredMsg(receivedMsgList.size() + 1);
//                                    if (receivedMsg == null) {
//                                        errorChatTxt.setText("Error msg from " + guestname);
//                                    } else {
//                                        if (receivedMsg.equals("EXIT")) {
//                                            disconnected = true;
//                                            handler.post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    guestNameTxt.setText(guestname + ": Disconnected!");
////                                                    sendBtn.setEnabled(false);
//                                                }
//                                            });
//                                        } else if(receivedMsg.contains("File")) {
//                                            receivedMsgList.add(receivedMsg);
//                                            String fileMsg = receivedMsg.replaceAll("File ", "");
//                                            String[] infoFile = fileMsg.split("---", 2);
//                                            String fileName = infoFile[0].trim();
//                                            log("File " + fileName, guestname, false, disconnected);
//                                        } else {
//                                            receivedMsgList.add(receivedMsg);
//                                            log(receivedMsg, guestname, false, disconnected);
//                                        }
//                                    }
//                                }
//                            }
//
//                            in.close();
//                            out.close();
//                            socket.close();
//                            if (serverSocket != null) serverSocket.close();
//
//                        } else {
//                            disconnected = true;
//                        }
//                    }
//                    if(!disconnected) {
//                        in.close();
//                        out.close();
//                        socket.close();
//                        if (serverSocket != null) serverSocket.close();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        t1.start();

        startUDPServer();
        startAutoSender();

        chatView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ChatView chatViewObj = (ChatView) chatView.getItemAtPosition(position);
//                String msg = chatViewObj.getContent();
                String msg = (String) chatView.getItemAtPosition(position);
                if(msg.contains("File") && msg.contains(guestname)) {
                    msg = msg.split("//", 2)[1].trim();
                    String fileName = msg.replaceAll("File ", "");
                    builder.setTitle("Confirmation").setMessage("Do you want to download '" + fileName + "'?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Iterator i = receivedMsgList.iterator();
//                            while(i.hasNext()) {
//                                String receivedMsg = (String) i.next();
//                                if(receivedMsg.contains(fileName)) {
//                                    String fileMsg = receivedMsg.replaceAll("File ", "");
//                                    String[] infoFile = fileMsg.split("--", 2);
//                                    byte[] fileStr = Base64.getDecoder().decode(infoFile[1].trim());
//
//                                    try {
//                                        String downloadDir = "/sdcard/Download/";
//                                        fos = new FileOutputStream(downloadDir + fileName);
//                                        fos.write(fileStr);
//                                        fos.flush();
//                                        fos.close();
//                                        Toast.makeText(parent.getContext(), "Save file in " + downloadDir + "!", Toast.LENGTH_SHORT).show();
//                                    } catch (Exception e) {
//                                        errorChatTxt.setText("Error! Cannot download file");
//                                        Toast.makeText(parent.getContext(), "Download failed!", Toast.LENGTH_SHORT).show();
//                                    }
//                                    break;
//                                }
//                            }
                            if(fileDataMap.containsKey(fileName)) {
                                String[] fileContent = fileDataMap.get(fileName);

                                if(!containNull(fileContent)) {
                                    String fileData = "";
                                    for(String s: fileContent)
                                        fileData += s;
                                    byte[] fileStr = Base64.getDecoder().decode(fileData);
//
                                    try {
                                        String downloadDir = "/sdcard/Download/";
                                        fos = new FileOutputStream(downloadDir + fileName);
                                        fos.write(fileStr);
                                        fos.flush();
                                        fos.close();
                                        Toast.makeText(parent.getContext(), "Save file in " + downloadDir + "!", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        errorChatTxt.setText("Error! Cannot download file");
                                        Toast.makeText(parent.getContext(), "Download failed!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String sendMsg = chatTxt.getText().toString();
                    if(!sendMsg.isEmpty()) {
//                        sendMsgList.add(sendMsg);
//                        if(!disconnected && socket.isConnected()) {
//                            if(sendMsgList.size() != 0) {
//                                Iterator i = sendMsgList.iterator();
//                                while(i.hasNext()) {
//                                    String msg = (String) i.next();
//                                    Message msgObj = new Message(msg, username, priKey, guestPubKey);
//                                    tagNumber++;
//                                    String securedMsg = msgObj.createSecuredMsg(tagNumber);
//                                    out.write(securedMsg);
//                                    out.newLine();
//                                    out.flush();
//
//                                    i.remove();
//                                }
//                            }
//                        }
                        Message msgObj = new Message(sendMsg, username, priKey, guestPubKey, tagNumber);
                        sendMsgList.add(msgObj);

//                        ChatView chatViewObj = new ChatView(tagNumber, username, sendMsg);
//                        log(chatViewObj, true, true);
                        log(tagNumber + "//" + sendMsg, true, true);
                        tagNumber++;

//                        sendMsgList.removeAll(removeMsgList);
//                        removeMsgList.clear();
//                        Iterator i = sendMsgList.iterator();
//                        while(i.hasNext()) {
//                            msgObj = (Message) i.next();
//                            String securedMsg = msgObj.createSecuredMsg();
//                            startUDPSender(securedMsg);
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        exitChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sendMsgList.isEmpty()) {
                    builder.setTitle("Confirmation").setMessage("Some messages haven't sent yet. Do you want to exit?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            if(!disconnected && socket.isConnected()) {
//                                try {
//                                    Message msgObj = new Message("EXIT", username, priKey, guestPubKey);
//                                    tagNumber++;
//                                    String exitMsg = msgObj.createSecuredMsg(tagNumber);
//                                    out.write(exitMsg);
//                                    out.newLine();
//                                    out.flush();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
                            try {
                                Message msgObj = new Message("EXIT", username, priKey, guestPubKey, tagNumber);
                                tagNumber++;
                                String exitMsg = msgObj.createSecuredMsg();
                                startUDPSender(exitMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            isExit = true;
                            Intent newIntent = new Intent(ChatActivity.this, ChatMainActivity.class);
                            newIntent.putExtra("PRI_KEY", priKeyStr);
                            newIntent.putExtra("USERNAME", username);
                            ChatActivity.this.startActivity(newIntent);
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
//                    if(!disconnected && socket.isConnected()) {
//                        try {
//                            Message msgObj = new Message("EXIT", username, priKey, guestPubKey);
//                            tagNumber++;
//                            String exitMsg = msgObj.createSecuredMsg(tagNumber);
//                            out.write(exitMsg);
//                            out.newLine();
//                            out.flush();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                    }

                    try {
                        Message msgObj = new Message("EXIT", username, priKey, guestPubKey, tagNumber);
                        String exitMsg = msgObj.createSecuredMsg();
                        startUDPSender(exitMsg);
                        tagNumber++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    isExit = true;
                    Intent newIntent = new Intent(ChatActivity.this, ChatMainActivity.class);
                    newIntent.putExtra("PRI_KEY", priKeyStr);
                    newIntent.putExtra("USERNAME", username);
                    ChatActivity.this.startActivity(newIntent);
                }
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                File file = fragment.getFile();
//                System.out.println(file.getName());
                if(file == null)
                    return;

                try {
                    byte[] bytes = new byte[(int) file.length()];
                    fis = new FileInputStream(file);
                    fis.read(bytes, 0, bytes.length);
                    String fileStr = Base64.getEncoder().encodeToString(bytes);
                    uploadBtn.setEnabled(false);
                    errorChatTxt.setText("Uploading file...");

                    int size = 16384; //2^14 bytes
                    int numMsg = (int) fileStr.length()/size;
                    for(int start = 0; start < fileStr.length(); start += size) {
                        int index = (int) start/size;
                        String subFileStr = fileStr.substring(start, Math.min(fileStr.length(), start + size));
                        String fileNameMsg = "FILE " + file.getName() + "::" + index + "::" + numMsg + "::" + subFileStr;
//                        System.out.println(fileNameMsg);
                        Message msgObj = new Message(fileNameMsg, username, priKey, guestPubKey, tagNumber);
                        sendMsgList.add(msgObj);
                        tagNumber++;
                    }
//                    sendMsgList.add(fileNameMsg);
//                    if(!disconnected && socket.isConnected()) {
//                        // Send name and bytes of file
//                        if(sendMsgList.size() != 0) {
//                            Iterator i = sendMsgList.iterator();
//                            while(i.hasNext()) {
//                                try {
//                                    String msg = (String) i.next();
//                                    Message msgObj = new Message(msg, username, priKey, guestPubKey);
//                                    tagNumber++;
//                                    String securedMsg = msgObj.createSecuredMsg(tagNumber);
//                                    out.write(securedMsg);
//                                    out.newLine();
//                                    out.flush();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                i.remove();
//                            }
//                        }
//                        fis.close();
//                        log("File " + file.getName(), username, true, disconnected);
//                    }

                    fis.close();
//                    ChatView chatViewObj = new ChatView(tagNumber, username, "File " + file.getName());
//                    log(chatViewObj, true, true);
                    log("File " + file.getName(), true, true);

//                    sendMsgList.removeAll(removeMsgList);
//                    removeMsgList.clear();
//                    Iterator i = sendMsgList.iterator();
//                    while(i.hasNext()) {
//                        Message msgObj = (Message) i.next();
//                        String securedMsg = msgObj.createSecuredMsg();
//                        startUDPSender(securedMsg);
//                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}