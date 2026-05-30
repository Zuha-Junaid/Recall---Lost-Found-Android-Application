package com.lostnfound.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemDetailActivity extends AppCompatActivity {
    private MaterialToolbar toolbarDetail;
    private TextView txtDetailCategory, txtDetailStatus, txtDetailTitle, txtDetailLocation, txtDetailAuthor, txtDetailDate, txtDetailDescription;
    private ImageView imgDetailPhoto;
    private View layoutVerificationWidget, layoutAnswerInput;
    private TextView txtDetailQuestion;
    private EditText inputClaimAnswer;
    private MaterialButton btnSubmitVerification, btnChat, btnActionResolve, btnActionDelete, btnActionEdit;
    private ItemModel selectedItem;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFirebaseEnabled = false;
    private String currentUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        selectedItem = (ItemModel) getIntent().getSerializableExtra("selected_item");
        if (selectedItem == null) { finish(); return; }
        
        toolbarDetail = findViewById(R.id.toolbarDetail);
        txtDetailCategory = findViewById(R.id.txtDetailCategory);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtDetailTitle = findViewById(R.id.txtDetailTitle);
        txtDetailLocation = findViewById(R.id.txtDetailLocation);
        txtDetailAuthor = findViewById(R.id.txtDetailAuthor);
        txtDetailDate = findViewById(R.id.txtDetailDate);
        txtDetailDescription = findViewById(R.id.txtDetailDescription);
        imgDetailPhoto = findViewById(R.id.imgDetailPhoto);
        layoutVerificationWidget = findViewById(R.id.layoutVerificationWidget);
        layoutAnswerInput = findViewById(R.id.layoutAnswerInput);
        txtDetailQuestion = findViewById(R.id.txtDetailQuestion);
        inputClaimAnswer = findViewById(R.id.inputClaimAnswer);
        btnSubmitVerification = findViewById(R.id.btnSubmitVerification);
        btnChat = findViewById(R.id.btnChat);
        btnActionResolve = findViewById(R.id.btnActionResolve);
        btnActionDelete = findViewById(R.id.btnActionDelete);
        btnActionEdit = findViewById(R.id.btnActionEdit);
        
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            isFirebaseEnabled = true;
            if (mAuth.getCurrentUser() != null) currentUid = mAuth.getCurrentUser().getUid();
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        
        if (!isFirebaseEnabled) {
            currentUid = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("emu_user_uid", "emu_uid_999");
        }
        
        setSupportActionBar(toolbarDetail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarDetail.setNavigationOnClickListener(v -> finish());
        
        bindDetails();
    }

    private void bindDetails() {
        txtDetailTitle.setText(selectedItem.getTitle());
        txtDetailCategory.setText(selectedItem.getCategory().toUpperCase());
        txtDetailLocation.setText("Location: " + selectedItem.getLocation());
        txtDetailAuthor.setText("Reporter: " + selectedItem.getPostedByName());
        txtDetailDate.setText("Published: " + selectedItem.getDateString());
        txtDetailDescription.setText(selectedItem.getDescription());
        txtDetailStatus.setText(selectedItem.getStatus());
        
        String b64 = selectedItem.getBase64Image();
        if (b64 != null && !b64.isEmpty()) {
            try {
                byte[] dec = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                if (bmp != null) imgDetailPhoto.setImageBitmap(bmp);
            } catch (Exception ignored) {}
        }
        
        boolean isMine = currentUid.equalsIgnoreCase(selectedItem.getPostedByUid());
        if (isMine) {
            layoutVerificationWidget.setVisibility(View.GONE);
            btnChat.setVisibility(View.GONE);
            btnActionDelete.setVisibility(View.VISIBLE);
            btnActionEdit.setVisibility(View.VISIBLE);
            btnActionResolve.setVisibility("RETURNED".equalsIgnoreCase(selectedItem.getStatus()) ? View.GONE : View.VISIBLE);
            
            btnActionResolve.setOnClickListener(v -> markResolved());
            btnActionDelete.setOnClickListener(v -> deleteReport());
            btnActionEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, PostItemActivity.class);
                intent.putExtra("edit_item", selectedItem);
                startActivity(intent);
                finish(); // Close detail so user sees fresh list after edit
            });
        } else {
            btnActionDelete.setVisibility(View.GONE);
            btnActionEdit.setVisibility(View.GONE);
            btnActionResolve.setVisibility(View.GONE);
            btnChat.setVisibility(View.VISIBLE);
            
            btnChat.setOnClickListener(v -> {
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra("item_id", selectedItem.getId());
                i.putExtra("item_title", selectedItem.getTitle());
                i.putExtra("posted_by_uid", selectedItem.getPostedByUid());
                i.putExtra("posted_by_name", selectedItem.getPostedByName());
                startActivity(i);
            });
            
            String q = selectedItem.getVerificationQuestion();
            if (q != null && !q.isEmpty()) {
                layoutVerificationWidget.setVisibility(View.VISIBLE);
                txtDetailQuestion.setText(q);
                btnSubmitVerification.setOnClickListener(v -> {
                    Toast.makeText(this, "Proof credentials submitted!", Toast.LENGTH_SHORT).show();
                    layoutAnswerInput.setVisibility(View.GONE);
                    btnSubmitVerification.setEnabled(false);
                });
            } else {
                layoutVerificationWidget.setVisibility(View.GONE);
            }
        }
    }

    private void markResolved() {
        if (isFirebaseEnabled) {
            db.collection("items").document(selectedItem.getId()).update("status", "RETURNED").addOnSuccessListener(a -> finish());
        } else {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            try {
                JSONArray arr = new JSONArray(prefs.getString("emulated_items_db", "[]"));
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj.getString("id").equalsIgnoreCase(selectedItem.getId())) { obj.put("status", "RETURNED"); break; }
                }
                prefs.edit().putString("emulated_items_db", arr.toString()).apply();
                finish();
            } catch (Exception ignored) {}
        }
    }

    private void deleteReport() {
        if (isFirebaseEnabled) {
            db.collection("items").document(selectedItem.getId()).delete().addOnSuccessListener(a -> finish());
        } else {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            try {
                JSONArray arr = new JSONArray(prefs.getString("emulated_items_db", "[]"));
                JSONArray news = new JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (!obj.getString("id").equalsIgnoreCase(selectedItem.getId())) news.put(obj);
                }
                prefs.edit().putString("emulated_items_db", news.toString()).apply();
                finish();
            } catch (Exception ignored) {}
        }
    }
}
