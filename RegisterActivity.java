package com.lostnfound.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, passwordInput, deptInput;
    private MaterialButton btnSignUp;
    private TextView txtLoginChoice; // Fixed: Use TextView to match layout
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFirebaseEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        deptInput = findViewById(R.id.deptInput);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLoginChoice = findViewById(R.id.txtLoginChoice);
        
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            isFirebaseEnabled = true;
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        
        btnSignUp.setOnClickListener(v -> handleRegistration());
        txtLoginChoice.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String dept = deptInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)) { nameInput.setError("Required"); return; }
        if (TextUtils.isEmpty(email)) { emailInput.setError("Required"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) { passwordInput.setError("Min 6 Characters"); return; }
        if (TextUtils.isEmpty(dept)) { deptInput.setError("Required"); return; }
        
        btnSignUp.setEnabled(false);
        
        if (isFirebaseEnabled) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(uid)
                            .set(new UserModel(uid, name, email, dept))
                            .addOnCompleteListener(t -> goToMain())
                            .addOnFailureListener(e -> {
                                btnSignUp.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    btnSignUp.setEnabled(true);
                    String error = task.getException() != null ? task.getException().getMessage() : "Registration Failed";
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Emulator / Offline fallback
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putBoolean("is_logged_in_emu", true)
                    .putString("emu_user_email", email)
                    .putString("emu_user_name", name)
                    .putString("emu_user_uid", "emu_uid_" + name.hashCode())
                    .putString("emu_user_dept", dept)
                    .apply();
            goToMain();
        }
    }

    private void goToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}
