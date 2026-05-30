package com.lostnfound.app;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView txtWelcomeUser, txtUserDepartment;
    private View btnLogout, emptyStateLayout;
    private EditText searchEditText;
    private MaterialButton btnTabLost, btnTabFound;
    private RecyclerView itemsRecyclerView;
    private ProgressBar mainProgressBar;
    private FloatingActionButton fabPostItem;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFirebaseEnabled = false;
    private ListenerRegistration itemsListener;
    private final List<ItemModel> allItems = new ArrayList<>();
    private final List<ItemModel> filteredItems = new ArrayList<>();
    private ItemsAdapter itemsAdapter;
    private String selectedTabType = "LOST";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtWelcomeUser = findViewById(R.id.txtWelcomeUser);
        txtUserDepartment = findViewById(R.id.txtUserDepartment);
        btnLogout = findViewById(R.id.btnLogout);
        searchEditText = findViewById(R.id.searchEditText);
        btnTabLost = findViewById(R.id.btnTabLost);
        btnTabFound = findViewById(R.id.btnTabFound);
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        mainProgressBar = findViewById(R.id.mainProgressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        fabPostItem = findViewById(R.id.fabPostItem);
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            isFirebaseEnabled = true;
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        if (!checkUserSession()) return;
        setupUserData();
        setupRecyclerView();
        setupListeners();
        loadItemsData();
    }
    private boolean checkUserSession() {
        if (isFirebaseEnabled) {
            if (mAuth.getCurrentUser() == null) { goToLogin(); return false; }
        } else {
            if (!getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_logged_in_emu", false)) { goToLogin(); return false; }
        }
        return true;
    }
    private void setupUserData() {
        if (isFirebaseEnabled && mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(snap -> {
                if (snap.exists()) {
                    txtWelcomeUser.setText("Hello, " + snap.getString("name") + "!");
                    txtUserDepartment.setText(snap.getString("department"));
                }
            });
        } else {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            txtWelcomeUser.setText("Hello, " + prefs.getString("emu_user_name", "Developer") + "!");
            txtUserDepartment.setText(prefs.getString("emu_user_dept", "CS Student") + " (Simulated)");
        }
    }
    private void setupRecyclerView() {
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapter = new ItemsAdapter(this, filteredItems, item -> {
            Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
            intent.putExtra("selected_item", item);
            startActivity(intent);
        });
        itemsRecyclerView.setAdapter(itemsAdapter);
    }
    private void setupListeners() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnTabLost.setOnClickListener(v -> { selectedTabType = "LOST"; updateTabSelectionUI(); applyFilters(); });
        btnTabFound.setOnClickListener(v -> { selectedTabType = "FOUND"; updateTabSelectionUI(); applyFilters(); });
        fabPostItem.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PostItemActivity.class)));
        searchEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }
            public void afterTextChanged(Editable s) {}
        });
        updateTabSelectionUI();
    }
    private void updateTabSelectionUI() {
        if ("LOST".equalsIgnoreCase(selectedTabType)) {
            btnTabLost.setStrokeColorResource(R.color.purple_500);
            btnTabLost.setStrokeWidth(3);
            btnTabLost.setBackgroundColor(getColor(R.color.purple_200));
            btnTabLost.setTextColor(getColor(R.color.purple_700));
            btnTabFound.setStrokeColorResource(R.color.border_color);
            btnTabFound.setStrokeWidth(3);
            btnTabFound.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btnTabFound.setTextColor(Color.parseColor("#49454F"));
        } else {
            btnTabFound.setStrokeColorResource(R.color.purple_500);
            btnTabFound.setStrokeWidth(3);
            btnTabFound.setBackgroundColor(getColor(R.color.purple_200));
            btnTabFound.setTextColor(getColor(R.color.purple_700));
            btnTabLost.setStrokeColorResource(R.color.border_color);
            btnTabLost.setStrokeWidth(3);
            btnTabLost.setBackgroundColor(Color.parseColor("#FFFFFF"));
            btnTabLost.setTextColor(Color.parseColor("#49454F"));
        }
    }
    private void loadItemsData() {
        mainProgressBar.setVisibility(View.VISIBLE);
        if (isFirebaseEnabled) {
            itemsListener = db.collection("items").orderBy("dateString", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        mainProgressBar.setVisibility(View.GONE);
                        if (value != null) {
                            allItems.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                ItemModel item = doc.toObject(ItemModel.class);
                                if (item != null) { item.setId(doc.getId()); allItems.add(item); }
                            }
                        }
                        applyFilters();
                    });
        } else {
            mainProgressBar.setVisibility(View.GONE);
            loadEmulatedItems();
        }
    }
    private void loadEmulatedItems() {
        allItems.clear();
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String itemsJson = prefs.getString("emulated_items_db", "");
        if (itemsJson.isEmpty()) {
            List<ItemModel> sampleList = new ArrayList<>();
            sampleList.add(new ItemModel("sample_1", "Space Grey iPad Pro", "Forgot inside calculus room auditorium on May 28.", "Electronics", "Sci Auditorium Block", null, "LOST", "demo_id_101", "Amina Malik", "May 28, 2026", "What's the unique color of the keyboard cover?"));
            sampleList.add(new ItemModel("sample_2", "Leather Wallet", "Found a brown leather wallet near campus entrance.", "Accessories", "Main Entrance gate", null, "FOUND", "demo_id_102", "Hamza Javed", "May 27, 2026", "Which bank credit card is inside?"));
            sampleList.add(new ItemModel("sample_3", "Gold Keychain", "Lost keychain containing 3 keys.", "Keys", "Mechanical Block Cafe", null, "LOST", "demo_id_103", "Zainab Bibi", "May 26, 2026", "What's on the tag?"));
            JSONArray arr = new JSONArray();
            try {
                for (ItemModel item : sampleList) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", item.getId()).put("title", item.getTitle()).put("description", item.getDescription()).put("category", item.getCategory()).put("location", item.getLocation()).put("base64Image", "").put("status", item.getStatus()).put("postedByUid", item.getPostedByUid()).put("postedByName", item.getPostedByName()).put("dateString", item.getDateString()).put("verificationQuestion", item.getVerificationQuestion());
                    arr.put(obj); allItems.add(item);
                }
                prefs.edit().putString("emulated_items_db", arr.toString()).apply();
            } catch (Exception ignored) {}
        } else {
            try {
                JSONArray arr = new JSONArray(itemsJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    allItems.add(new ItemModel(obj.getString("id"), obj.getString("title"), obj.getString("description"), obj.getString("category"), obj.getString("location"), obj.optString("base64Image", ""), obj.getString("status"), obj.getString("postedByUid"), obj.getString("postedByName"), obj.getString("dateString"), obj.getString("verificationQuestion")));
                }
            } catch (Exception ignored) {}
        }
        applyFilters();
    }
    private void applyFilters() {
        filteredItems.clear();
        for (ItemModel item : allItems) {
            boolean matches = "LOST".equalsIgnoreCase(selectedTabType) ? "LOST".equalsIgnoreCase(item.getStatus()) : ("FOUND".equalsIgnoreCase(item.getStatus()) || "RETURNED".equalsIgnoreCase(item.getStatus()));
            if (matches) {
                if (searchQuery.isEmpty() || item.getTitle().toLowerCase().contains(searchQuery) || item.getCategory().toLowerCase().contains(searchQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        itemsAdapter.updateList(filteredItems);
        emptyStateLayout.setVisibility(filteredItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
    private void handleLogout() {
        if (isFirebaseEnabled) mAuth.signOut();
        else getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("is_logged_in_emu", false).apply();
        goToLogin();
    }
    private void goToLogin() { startActivity(new Intent(this, LoginActivity.class)); finish(); }
    protected void onResume() { super.onResume(); if (!isFirebaseEnabled) loadEmulatedItems(); }
    protected void onDestroy() { super.onDestroy(); if (itemsListener != null) itemsListener.remove(); }
}