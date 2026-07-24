package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SignUp extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private ImageView ivPasswordVisibility;
    private LinearLayout llEmailContainer, llPasswordContainer;
    private TextView tvEmailError, tvPasswordError;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        llEmailContainer = findViewById(R.id.llEmailContainer);
        llPasswordContainer = findViewById(R.id.llPasswordContainer);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        MaterialButton btnGuest = findViewById(R.id.btnGuest);

        // Password Visibility Toggle
        ivPasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide Password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivPasswordVisibility.setImageResource(R.drawable.ic_visibility);
            } else {
                // Show Password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivPasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length()); // Keep cursor at the end
        });

        // Email Validation Logic (Real-time)
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                if (email.contains("@")) {
                    // Valid
                    llEmailContainer.setBackgroundResource(R.drawable.edit_text_bg);
                    tvEmailError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password Validation Logic (Real-time)
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidPassword(s.toString())) {
                    llPasswordContainer.setBackgroundResource(R.drawable.edit_text_bg);
                    tvPasswordError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Create Account Button Logic
        btnCreateAccount.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean isValid = true;

            // Email validation check on click
            if (!email.contains("@")) {
                llEmailContainer.setBackgroundResource(R.drawable.edit_text_error_bg);
                tvEmailError.setVisibility(View.VISIBLE);
                isValid = false;
            }

            // Password validation check on click
            if (!isValidPassword(password)) {
                llPasswordContainer.setBackgroundResource(R.drawable.edit_text_error_bg);
                tvPasswordError.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (isValid) {
                // Save to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fullName", fullName);
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putBoolean("isGuest", false);
                editor.apply();

                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignUp.this, onboarding4.class);
                startActivity(intent);
                finish();
            }
        });

        // Sign In Text Logic
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Sign_in.class);
            startActivity(intent);
        });

        // Continue as Guest Logic
        btnGuest.setOnClickListener(v -> {
            // Save as Guest
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isGuest", true);
            editor.putString("fullName", "Guest User");
            // Clear any previous major when entering as guest
            editor.remove("academicMajor");
            editor.apply();

            Intent intent = new Intent(SignUp.this, onboarding4.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        String symbols = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (symbols.contains(String.valueOf(c))) hasSymbol = true;
        }

        return hasLetter && hasDigit && hasSymbol;
    }
}