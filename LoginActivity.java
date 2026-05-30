package com.lostnfound.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private MaterialButton btnSignIn;
    private View txtRegisterChoice;
    private FirebaseAuth mAuth;
    private boolean isFirebaseEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSignIn = findViewById(R.id.btnSignIn);
        txtRegisterChoice = findViewById(R.id.txtRegisterChoice);
        try {
            if (FirebaseApp.getApps(this).isEmpty()) FirebaseApp.initializeApp(this);
            mAuth = FirebaseAuth.getInstance();
            isFirebaseEnabled = true;
        } catch (Exception e) {
            isFirebaseEnabled = false;
        }
        if (isFirebaseEnabled && mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }
        btnSignIn.setOnClickListener(v -> loginUser());
        txtRegisterChoice.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(email)) { emailInput.setError("Required"); return; }
        if (TextUtils.isEmpty(password)) { passwordInput.setError("Required"); return; }
        btnSignIn.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
        if (isFirebaseEnabled) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                btnSignIn.setEnabled(true);
                if (task.isSuccessful()) goToMain();
                else Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            });
        } else {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putBoolean("is_logged_in_emu", true)
                    .putString("emu_user_email", email)
                    .putString("emu_user_name", email.split("@")[0])
                    .putString("emu_user_uid", "emu_uid_123456")
                    .putString("emu_user_dept", "CS Student Council")
                    .apply();
            goToMain();
        }
    }
    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}