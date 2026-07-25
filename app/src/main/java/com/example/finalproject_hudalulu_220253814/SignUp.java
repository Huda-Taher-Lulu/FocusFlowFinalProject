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

import com.example.finalproject_hudalulu_220253814.database.UserDbHelper;
import com.example.finalproject_hudalulu_220253814.models.User;
import com.google.android.material.button.MaterialButton;

public class SignUp extends AppCompatActivity {

    private EditText etFullName, etUsername, etEmail, etPassword, etPhone, etAddress;
    private ImageView ivPasswordVisibility;
    private LinearLayout llEmailContainer, llPasswordContainer;
    private TextView tvEmailError, tvPasswordError;
    private boolean isPasswordVisible = false;
    private UserDbHelper userDbHelper;

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

        userDbHelper = new UserDbHelper(this);

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
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
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivPasswordVisibility.setImageResource(R.drawable.ic_visibility);
            } else {
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

        // Create Account Button Logic
        btnCreateAccount.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean isValid = true;

            if (!email.contains("@")) {
                llEmailContainer.setBackgroundResource(R.drawable.edit_text_error_bg);
                tvEmailError.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (username.length() < 3) {
                Toast.makeText(this, R.string.error_username_short, Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValidPassword(password)) {
                llPasswordContainer.setBackgroundResource(R.drawable.edit_text_error_bg);
                tvPasswordError.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()
                    || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) return;

            // Requirement 3.3: persist the new account in SQLite (Users database)
            if (userDbHelper.emailExists(email)) {
                llEmailContainer.setBackgroundResource(R.drawable.edit_text_error_bg);
                tvEmailError.setVisibility(View.VISIBLE);
                Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userDbHelper.usernameExists(username)) {
                Toast.makeText(this, "This username is already taken", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User(fullName, username, email, phone, address, password);
            long newUserId = userDbHelper.insertUser(newUser);

            if (newUserId == -1) {
                Toast.makeText(this, "Could not create account. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // SharedPreferences now only holds the session pointer + a quick-access cache;
            // the actual account record lives in the Users SQLite database.
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("currentUserId", newUserId);
            editor.putString("fullName", fullName);
            editor.putString("username", username);
            editor.putString("email", email);
            editor.putString("phone", phone);
            editor.putString("address", address);
            editor.putBoolean("isGuest", false);
            editor.apply();

            Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignUp.this, onboarding4.class);
            startActivity(intent);
            finish();
        });

        // Sign In Text Logic
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Sign_in.class);
            startActivity(intent);
        });

        // Continue as Guest Logic
        btnGuest.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isGuest", true);
            editor.putLong("currentUserId", -1);
            editor.putString("fullName", "Guest User");
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