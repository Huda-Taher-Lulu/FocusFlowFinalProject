package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class settings extends AppCompatActivity {

    private SharedPreferences prefs;
    private Slider sliderSensitivity;
    private TextView tvSensitivityLevel;
    private SwitchMaterial switchFlipVibration, switchAutoStartBreaks, switchDailyReminders;
    private Spinner spinnerAmbientSound;
    private TextView tvThemeLight, tvThemeDark, tvThemeSystem;
    private final String[] soundOptions = {"Lofi Rain", "Calm Forest", "Ocean Waves", "Silent"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        // Header
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Profile
        View cvProfile = findViewById(R.id.cvProfile);
        cvProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, Profile.class));
        });

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileName.setText(prefs.getString("fullName", "Alex Rivera"));

        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);
        String imagePath = prefs.getString("profileImageUri", null);
        if (imagePath != null) {
            Glide.with(this).load(imagePath).placeholder(R.drawable.ic_person).into(ivProfile);
        }

        // Sensor & Gesture
        sliderSensitivity = findViewById(R.id.sliderSensitivity);
        tvSensitivityLevel = findViewById(R.id.tvSensitivityLevel);
        switchFlipVibration = findViewById(R.id.switchFlipVibration);

        // Focus Preferences
        switchAutoStartBreaks = findViewById(R.id.switchAutoStartBreaks);
        spinnerAmbientSound = findViewById(R.id.spinnerAmbientSound);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, soundOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmbientSound.setAdapter(adapter);

        // Appearance
        tvThemeLight = findViewById(R.id.tvThemeLight);
        tvThemeDark = findViewById(R.id.tvThemeDark);
        tvThemeSystem = findViewById(R.id.tvThemeSystem);
        switchDailyReminders = findViewById(R.id.switchDailyReminders);

        // Logout
        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, onboarding4.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadSettings() {
        // Sensitivity
        float sensitivity = prefs.getFloat("sensorSensitivity", 50.0f);
        sliderSensitivity.setValue(sensitivity);
        updateSensitivityUI(sensitivity);

        // Switches
        switchFlipVibration.setChecked(prefs.getBoolean("flipVibrationSoundEnabled", true));
        switchAutoStartBreaks.setChecked(prefs.getBoolean("autoStartBreaks", false));
        switchDailyReminders.setChecked(prefs.getBoolean("dailyReminders", true));

        // Ambient Sound
        String currentSound = prefs.getString("ambientSoundName", "Lofi Rain");
        int selection = Arrays.asList(soundOptions).indexOf(currentSound);
        if (selection != -1) spinnerAmbientSound.setSelection(selection);

        // Theme
        int theme = prefs.getInt("themeMode", 0); 
        updateThemeUI(theme);
    }

    private void setupListeners() {
        // Sensitivity Slider
        sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            updateSensitivityUI(value);
            prefs.edit().putFloat("sensorSensitivity", value).apply();
        });

        // Switches
        switchFlipVibration.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("flipVibrationSoundEnabled", isChecked).apply());
        
        switchAutoStartBreaks.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("autoStartBreaks", isChecked).apply());
        
        switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("dailyReminders", isChecked).apply());

        // Ambient Sound Spinner
        spinnerAmbientSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = soundOptions[position];
                prefs.edit().putString("ambientSoundName", selected).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Theme Buttons
        tvThemeLight.setOnClickListener(v -> changeTheme(0));
        tvThemeDark.setOnClickListener(v -> changeTheme(1));
        tvThemeSystem.setOnClickListener(v -> changeTheme(2));
    }

    private void updateSensitivityUI(float value) {
        if (value == 0) {
            tvSensitivityLevel.setText("Low");
            tvSensitivityLevel.setTextColor(ContextCompat.getColor(this, R.color.text_red));
            tvSensitivityLevel.setBackgroundResource(R.drawable.badge_bg_red);
        } else if (value == 50) {
            tvSensitivityLevel.setText("Medium");
            tvSensitivityLevel.setTextColor(Color.parseColor("#B8860B"));
            tvSensitivityLevel.setBackgroundResource(R.drawable.badge_bg_yellow);
        } else {
            tvSensitivityLevel.setText("High");
            tvSensitivityLevel.setTextColor(ContextCompat.getColor(this, R.color.text_green));
            tvSensitivityLevel.setBackgroundResource(R.drawable.badge_bg_green);
        }
    }

    private void changeTheme(int mode) {
        prefs.edit().putInt("themeMode", mode).apply();
        updateThemeUI(mode);
        
        if (mode == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void updateThemeUI(int mode) {
        tvThemeLight.setBackgroundResource(0);
        tvThemeLight.setTextColor(getResources().getColor(R.color.text_gray));
        tvThemeDark.setBackgroundResource(0);
        tvThemeDark.setTextColor(getResources().getColor(R.color.text_gray));
        tvThemeSystem.setBackgroundResource(0);
        tvThemeSystem.setTextColor(getResources().getColor(R.color.text_gray));

        TextView selected = mode == 0 ? tvThemeLight : (mode == 1 ? tvThemeDark : tvThemeSystem);
        selected.setBackgroundResource(R.color.white);
        selected.setTextColor(getResources().getColor(R.color.black));
        // Removed setTypeface(BOLD) as per user request to keep text normal
    }
}
