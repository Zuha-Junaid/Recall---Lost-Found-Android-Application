package com.lostnfound.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class PostItemActivity extends AppCompatActivity {
    private MaterialToolbar toolbarPost;
    private MaterialButtonToggleGroup togglePostType;
    private View cardSelectImage, pickerInstructions;
    private ImageView imgSelectedPreview;
    private EditText inputTitle, inputLocation, inputDescription, inputVerification;
    private Spinner spinnerCategory;
    private MaterialButton btnSubmitReport;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFirebaseEnabled = false;
    private String selectedStatus = "LOST";
    private String base64ImageString = "";
    private final String[] categories = {"Electronics", "Documents", "Keys", "Books", "Accessories", "Instruments", "Others"};

    private boolean isEditMode = false;
    private String editItemId = "";

    // Modern ActivityResultLauncher for picking images
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    processSelectedImage(uri);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);
        
        toolbarPost = findViewById(R.id.toolbarPost);
        togglePostType = findViewById(R.id.togglePostType);
        cardSelectImage = findViewById(R.id.cardSelectImage);
        imgSelectedPreview = findViewById(R.id.imgSelectedPreview);
        pickerInstructions = findViewById(R.id.pickerInstructions);
        inputTitle = findViewById(R.id.inputTitle);
        inputLocation = findViewById(R.id.inputLocation);
        inputDescription = findViewById(R.id.inputDescription);
        inputVerification = findViewById(R.id.inputVerification);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            isFirebaseEnabled = true;
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        
        setupToolbar();
        setupSpinner();
        setupListeners();
        checkForEditMode();
    }

    private void checkForEditMode() {
        ItemModel itemToEdit = (ItemModel) getIntent().getSerializableExtra("edit_item");
        if (itemToEdit != null) {
            isEditMode = true;
            editItemId = itemToEdit.getId();
            toolbarPost.setTitle("Update Report");
            btnSubmitReport.setText("Update Report");
            inputTitle.setText(itemToEdit.getTitle());
            inputLocation.setText(itemToEdit.getLocation());
            inputDescription.setText(itemToEdit.getDescription());
            inputVerification.setText(itemToEdit.getVerificationQuestion());
            
            int catPos = Arrays.asList(categories).indexOf(itemToEdit.getCategory());
            if (catPos >= 0) spinnerCategory.setSelection(catPos);
            
            selectedStatus = itemToEdit.getStatus();
            if ("FOUND".equalsIgnoreCase(selectedStatus)) {
                togglePostType.check(R.id.toggleFound);
            } else {
                togglePostType.check(R.id.toggleLost);
            }
            
            base64ImageString = itemToEdit.getBase64Image();
            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                try {
                    byte[] dec = Base64.decode(base64ImageString, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    if (bmp != null) {
                        imgSelectedPreview.setImageBitmap(bmp);
                        imgSelectedPreview.setVisibility(View.VISIBLE);
                        pickerInstructions.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarPost);
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
        toolbarPost.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        togglePostType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) selectedStatus = (checkedId == R.id.toggleLost) ? "LOST" : "FOUND";
        });
        
        cardSelectImage.setOnClickListener(v -> {
            // Launch the modern photo picker
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        
        btnSubmitReport.setOnClickListener(v -> publishItemReport());
    }

    private void processSelectedImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                // Resize and compress for storage efficiency
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                float ratio = (float) width / (float) height;
                int finalWidth = 600;
                int finalHeight = (int) (600 / ratio);
                
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, finalWidth, finalHeight, true);
                imgSelectedPreview.setImageBitmap(scaled);
                imgSelectedPreview.setVisibility(View.VISIBLE);
                pickerInstructions.setVisibility(View.GONE);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                base64ImageString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void publishItemReport() {
        String title = inputTitle.getText().toString().trim();
        String loc = inputLocation.getText().toString().trim();
        String desc = inputDescription.getText().toString().trim();
        String ver = inputVerification.getText().toString().trim();
        
        if (title.isEmpty() || loc.isEmpty() || desc.isEmpty() || ver.isEmpty()) { 
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show(); 
            return; 
        }
        
        btnSubmitReport.setEnabled(false);
        String date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
        
        if (isFirebaseEnabled) {
            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "emu_uid";
            String uName = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail().split("@")[0] : "User";
            
            ItemModel item = new ItemModel(editItemId, title, desc, spinnerCategory.getSelectedItem().toString(), loc, base64ImageString, selectedStatus, uid, uName, date, ver);
            
            if (isEditMode) {
                db.collection("items").document(editItemId).set(item)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, "Report Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnSubmitReport.setEnabled(true);
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("items").add(item)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, "Report Published", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnSubmitReport.setEnabled(true);
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String itemsDb = prefs.getString("emulated_items_db", "[]");
            try {
                JSONArray arr = new JSONArray(itemsDb);
                JSONObject obj = new JSONObject();
                String finalId = isEditMode ? editItemId : "emu_item_" + System.currentTimeMillis();
                
                obj.put("id", finalId)
                   .put("title", title)
                   .put("description", desc)
                   .put("category", spinnerCategory.getSelectedItem().toString())
                   .put("location", loc)
                   .put("base64Image", base64ImageString)
                   .put("status", selectedStatus)
                   .put("postedByUid", prefs.getString("emu_user_uid", "emu_uid"))
                   .put("postedByName", prefs.getString("emu_user_name", "Developer"))
                   .put("dateString", date)
                   .put("verificationQuestion", ver);
                
                if (isEditMode) {
                    for (int i = 0; i < arr.length(); i++) {
                        if (arr.getJSONObject(i).getString("id").equals(editItemId)) {
                            arr.put(i, obj);
                            break;
                        }
                    }
                } else {
                    arr.put(obj);
                }
                
                prefs.edit().putString("emulated_items_db", arr.toString()).apply();
                finish();
            } catch (Exception ignored) {}
        }
    }
}
