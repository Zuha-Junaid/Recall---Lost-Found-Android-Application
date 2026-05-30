package com.lostnfound.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private TextView txtChatRecipient, txtItemSubContext;
    private RecyclerView chatRecyclerView;
    private EditText editChatMessage;
    private View btnSendChat, btnChatBack;
    private String itemId, itemTitle, postedByUid, postedByName, roomId, currentUid = "", currentUserName = "Me";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFirebaseEnabled = false;
    private ListenerRegistration chatListener;
    private final List<MessageModel> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        itemId = getIntent().getStringExtra("item_id");
        itemTitle = getIntent().getStringExtra("item_title");
        postedByUid = getIntent().getStringExtra("posted_by_uid");
        postedByName = getIntent().getStringExtra("posted_by_name");
        if (itemId == null || postedByUid == null) { finish(); return; }
        try {
            mAuth = FirebaseAuth.getInstance(); db = FirebaseFirestore.getInstance(); isFirebaseEnabled = true;
            if (mAuth.getCurrentUser() != null) {
                currentUid = mAuth.getCurrentUser().getUid(); currentUserName = mAuth.getCurrentUser().getEmail().split("@")[0];
            }
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        if (!isFirebaseEnabled) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            currentUid = prefs.getString("emu_user_uid", "emu_uid_999");
            currentUserName = prefs.getString("emu_user_name", "Local Staff");
        }
        roomId = itemId + "_" + (currentUid.compareTo(postedByUid) < 0 ? currentUid + "_" + postedByUid : postedByUid + "_" + currentUid);
        txtChatRecipient = findViewById(R.id.txtChatRecipient);
        txtItemSubContext = findViewById(R.id.txtItemSubContext);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editChatMessage = findViewById(R.id.editChatMessage);
        btnSendChat = findViewById(R.id.btnSendChat);
        btnChatBack = findViewById(R.id.btnChatBack);
        txtChatRecipient.setText(postedByName);
        txtItemSubContext.setText("Regard: " + itemTitle);
        chatAdapter = new ChatAdapter(this, messageList, currentUid);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        btnChatBack.setOnClickListener(v -> finish());
        btnSendChat.setOnClickListener(v -> sendMessage());
        loadMessages();
    }
    private void loadMessages() {
        if (isFirebaseEnabled) {
            chatListener = db.collection("chats").document(roomId).collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (value != null) {
                            messageList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                MessageModel m = doc.toObject(MessageModel.class);
                                if (m != null) messageList.add(m);
                            }
                            chatAdapter.notifyDataSetChanged();
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    });
        } else {
            loadEmulatedChats();
        }
    }
    private void loadEmulatedChats() {
        messageList.clear();
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String saved = prefs.getString("emu_chats_db_" + roomId, "");
        if (saved.isEmpty()) {
            messageList.add(new MessageModel(postedByUid, postedByName, "Hi, let me know if your proof matches so we can arrange physical handover on campus block-A.", System.currentTimeMillis() - 5000));
        } else {
            try {
                JSONArray arr = new JSONArray(saved);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    messageList.add(new MessageModel(obj.getString("senderId"), obj.getString("senderName"), obj.getString("text"), obj.getLong("timestamp")));
                }
            } catch (Exception ignored) {}
        }
        chatAdapter.notifyDataSetChanged();
    }
    private void sendMessage() {
        String uText = editChatMessage.getText().toString().trim();
        if (uText.isEmpty()) return;
        editChatMessage.setText("");
        MessageModel m = new MessageModel(currentUid, currentUserName, uText, System.currentTimeMillis());
        if (isFirebaseEnabled) {
            db.collection("chats").document(roomId).collection("messages").add(m);
        } else {
            messageList.add(m);
            chatAdapter.notifyDataSetChanged();
            saveEmulatedChats();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                messageList.add(new MessageModel(postedByUid, postedByName, "Thanks! Let me review this and update the resolved status.", System.currentTimeMillis()));
                chatAdapter.notifyDataSetChanged();
                saveEmulatedChats();
            }, 1000);
        }
    }
    private void saveEmulatedChats() {
        JSONArray arr = new JSONArray();
        try {
            for (MessageModel m : messageList) {
                arr.put(new JSONObject().put("senderId", m.getSenderId()).put("senderName", m.getSenderName()).put("text", m.getText()).put("timestamp", m.getTimestamp()));
            }
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putString("emu_chats_db_" + roomId, arr.toString()).apply();
        } catch (Exception ignored) {}
    }
    protected void onDestroy() { super.onDestroy(); if (chatListener != null) chatListener.remove(); }
}