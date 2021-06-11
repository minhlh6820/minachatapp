package com.example.minachatapp;

public class ChatView {
    private int tagNumber;
    private String username;
    private String content;
    private boolean status;

    public ChatView(int tagNumber, String username, String content) {
        this.tagNumber = tagNumber;
        this.username = username;
        this.content = content;
        this.status = false;
    }

    public int getTagNumber() {
        return this.tagNumber;
    }

    public String getUsername() {
        return this.username;
    }

    public String getContent() {
        return this.content;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
