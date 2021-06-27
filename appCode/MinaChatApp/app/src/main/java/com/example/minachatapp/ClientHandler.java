package com.example.minachatapp;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;

public class ClientHandler extends IoHandlerAdapter {
    private final Logger logger = Logger.getLogger(getClass());

    private boolean server = false;
    private boolean accepted = false;
    private ArrayList<String> friendsList = new ArrayList<String>();
    private ArrayList<String> requestedList = new ArrayList<String>();
    private String username = null;
    private String guestname = null;
    private String guestIP = null;
    private String guestPubKey = null;
    private String port = null;

    public ClientHandler(String username) {
        this.username = username;
    }

    public String getGuestname() {
        return this.guestname;
    }

    public String getGuestIP() {
        return this.guestIP;
    }

    public String getGuestPubKey() {
        return this.guestPubKey;
    }

    public String getPort() {
        return this.port;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public boolean isServer() { return this.server; }

    public void stopLogger() {
        Logger.shutdown();
    }

    public String[] getFriendsList() {
        String str[] = new String[friendsList.size()];
        for(int i=0; i<friendsList.size(); i++) {
            str[i] = friendsList.get(i);
        }
        return str;
    }

    public String[] getRequestedList() {
        String s[] = new String[requestedList.size()];
        for(int i=0; i<requestedList.size(); i++) {
            s[i] = requestedList.get(i);
        }
//        System.out.println(requestedList.size() + "--" + s.length);
        return s;
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        String str = message.toString().trim();
        logger.info(str);
        // List msg:
        // ACCEPT LOGIN ... / REJECT LOGIN ...
        // ACCEPT LOGOUT ...
        // FRIENDS LIST ...
        // ACCEPT CONNECT ...
        if(str.contains("REJECT")) {
            this.accepted = false;
        }

        if(str.contains("ACCEPT LOGIN")) {
            this.accepted = true;
        }

        if(str.contains("FRIENDS LIST")) {
            str = str.replaceFirst("FRIENDS LIST " + username + ": ", "");
            friendsList.clear();
            if(!str.equals("NULL")) {
                String[] list = str.split("--");
                for (String s: list) {
                    if(!friendsList.contains(s)) {
                        friendsList.add(s);
                    }
                }
            }
        }

        if(str.contains("REQUESTS LIST")) {
            str = str.replaceFirst("REQUESTS LIST " + username + ": ", "");
            requestedList.clear();
            if(!str.equals("NULL")) {
                String[] list = str.split("--");
                for (String s: list) {
                    if(!requestedList.contains(s)) {
                        requestedList.add(s);
                    }
                }
            }
        }

        if(str.contains("ACCEPT CONNECT")) {
            this.accepted = true;
            str = str.replaceFirst("ACCEPT CONNECT: ", "");
            String[] list = str.split("--");
            if(list[0].trim().equals(username)) {
                guestname = list[1].trim();
                server = true;
            } else {
                guestname = list[0].trim();
                server = false;
            }
            port = list[3].trim();
            guestIP = list[2].trim();
            guestPubKey = list[4].trim();
        }
        session.close();

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close();
    }
}
