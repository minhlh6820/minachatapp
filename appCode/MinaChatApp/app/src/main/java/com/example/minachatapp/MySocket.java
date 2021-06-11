package com.example.minachatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

public class MySocket {
//    private String serverIp = "fe80::5cba:fe5d:65cf:18d9";
//        private String serverIp = "fe80::54f9:2dc:65f3:15e4";
    private String serverIp = "fe80::78c:1956:9911:b3ec";
    private int serverPort = 9123;

    public MySocket() {

    }

    public MySocket(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public InetSocketAddress getServerSocket() throws IOException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//        System.out.println("Network: " + networkInterfaces);
        NetworkInterface wifiInterface = null;
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            // System.out.println(networkInterface.getDisplayName());
            if (networkInterface.getDisplayName().equals("wlan0")) {
                wifiInterface = networkInterface;
                break;
            }
        }
        InetAddress a = InetAddress.getByName(serverIp);
        Inet6Address dest = Inet6Address.getByAddress(null, a.getAddress(), wifiInterface );
        InetSocketAddress socket = new InetSocketAddress(dest, serverPort);
        return socket;
    }
}
