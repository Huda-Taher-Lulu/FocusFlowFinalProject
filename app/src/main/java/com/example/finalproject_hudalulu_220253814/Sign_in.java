package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Sign_in extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private ImageView ivPasswordVisibility;
    private LinearLayout llEmailContainer, llPasswordContainer;
    private TextView tvEmailError, tvPasswordError;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        llEmailContainer = findViewById(R.id.llEmailContainer);
        llPasswordContainer = findViewById(R.id.llPasswordContainer);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        MaterialButton btnApple = findViewById(R.id.btnApple); // This is actually Whatsapp in layout

        // Check for Remember Me data
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isRemembered = loginPrefs.getBoolean("rememberMe", false);
        if (isRemembered) {
            etEmail.setText(loginPrefs.getString("email", ""));
            etPassword.setText(loginPrefs.getString("password", ""));
            cbRememberMe.setChecked(true);
        }

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
            etPassword.setSelection(etPassword.getText().length());
        });

        // Email Validation Logic (Real-time)
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                if (email.contains("@")) {
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

        // Sign In Button Logic
        btnSignIn.setOnClickListener(v -> {
            String loginIdentifier = etEmail.getText().toString().trim(); // Can be email or username
            String password = etPassword.getText().toString().trim();

            boolean isValid = true;

            if (loginIdentifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (isValid) {
                // Check against SharedPreferences (Simple Login Simulation)
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String savedEmail = prefs.getString("email", "");
                String savedUsername = prefs.getString("username", "");
                String savedPassword = prefs.getString("password", "");

                boolean isCorrectIdentifier = loginIdentifier.equals(savedEmail) || loginIdentifier.equals(savedUsername);

                if (isCorrectIdentifier && password.equals(savedPassword)) {
                    // Handle Remember Me
                    SharedPreferences lPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor loginEditor = lPrefs.edit();
                    if (cbRememberMe.isChecked()) {
                        loginEditor.putBoolean("rememberMe", true);
                        loginEditor.putString("email", loginIdentifier);
                        loginEditor.putString("password", password);
                    } else {
                        loginEditor.clear();
                    }
                    loginEditor.apply();

                    Toast.makeText(this, R.string.sign_in_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Sign_in.this, onboarding4.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Forgot Password Logic
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_in.this, forget_password.class);
            startActivity(intent);
        });

        // Social Buttons Logic
        btnGoogle.setOnClickListener(v -> 
            Toast.makeText(this, R.string.firebase_needed, Toast.LENGTH_SHORT).show());
        
        btnApple.setOnClickListener(v -> 
            Toast.makeText(this, R.string.whatsapp_sign_in, Toast.LENGTH_SHORT).show());

        // Sign Up Text Logic
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_in.this, SignUp.class);
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