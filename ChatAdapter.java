package com.lostnfound.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final Context context;
    private final List<MessageModel> messageList;
    private final String currentUserId;

    public ChatAdapter(Context context, List<MessageModel> messageList, String currentUserId) {
        this.context = context; this.messageList = messageList; this.currentUserId = currentUserId;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_row, parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            holder.layoutOutgoing.setVisibility(View.VISIBLE);
            holder.layoutIncoming.setVisibility(View.GONE);
            holder.txtOutgoingMessage.setText(message.getText());
        } else {
            holder.layoutIncoming.setVisibility(View.VISIBLE);
            holder.layoutOutgoing.setVisibility(View.GONE);
            holder.txtIncomingSender.setText(message.getSenderName());
            holder.txtIncomingMessage.setText(message.getText());
        }
    }
    @Override
    public int getItemCount() { return messageList.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View layoutIncoming, layoutOutgoing;
        TextView txtIncomingSender, txtIncomingMessage, txtOutgoingMessage;
        public ViewHolder(@NonNull View v) {
            super(v);
            layoutIncoming = v.findViewById(R.id.layoutIncoming);
            layoutOutgoing = v.findViewById(R.id.layoutOutgoing);
            txtIncomingSender = v.findViewById(R.id.txtIncomingSender);
            txtIncomingMessage = v.findViewById(R.id.txtIncomingMessage);
            txtOutgoingMessage = v.findViewById(R.id.txtOutgoingMessage);
        }
    }
}