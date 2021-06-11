package com.example.minachatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatViewAdapter extends ArrayAdapter<ChatView> {
    public ChatViewAdapter(Context context, ArrayList<ChatView> chatViewList) {
        super(context, 0, chatViewList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentView = convertView;
        if(currentView == null) {
            currentView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_view, parent, false);
        }
        ChatView chatViewObj = getItem(position);

        TextView userChatView = currentView.findViewById(R.id.chatUser);
        userChatView.setText(chatViewObj.getUsername());

        TextView contentChatView = currentView.findViewById(R.id.chatContent);
        contentChatView.setText(chatViewObj.getContent());

        TextView statusChatView = currentView.findViewById(R.id.chatStatus);
        statusChatView.setText("" + chatViewObj.getStatus());

        return currentView;
    }
}
