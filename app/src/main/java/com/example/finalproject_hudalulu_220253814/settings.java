package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.Arrays;


public class settings extends AppCompatActivity {

    private SharedPreferences prefs;
    private Slider sliderSensitivity;
    private TextView tvSensitivityLevel;
    private SwitchMaterial switchFlipVibration, switchAutoStartBreaks, switchDailyReminders;
    private Spinner spinnerAmbientSound;
    private TextView tvThemeLight, tvThemeDark, tvThemeSystem, tvCurrentLanguage;
    private android.view.View llLanguage;
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
        // زر الرجوع
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // البروفايل
        findViewById(R.id.cvProfile).setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileName.setText(prefs.getString("fullName", ""));

        ShapeableImageView ivProfile = findViewById(R.id.ivProfile);
        String imagePath = prefs.getString("profileImageUri", null);
        if (imagePath != null) {
            com.bumptech.glide.Glide.with(this)
                .load(imagePath)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_person)
                .into(ivProfile);
        }

        // الحساسات والإيماءات
        sliderSensitivity = findViewById(R.id.sliderSensitivity);
        tvSensitivityLevel = findViewById(R.id.tvSensitivityLevel);
        switchFlipVibration = findViewById(R.id.switchFlipVibration);

        // إعدادات التركيز
        switchAutoStartBreaks = findViewById(R.id.switchAutoStartBreaks);
        spinnerAmbientSound = findViewById(R.id.spinnerAmbientSound);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, soundOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmbientSound.setAdapter(adapter);

        // المظهر
        tvThemeLight = findViewById(R.id.tvThemeLight);
        tvThemeDark = findViewById(R.id.tvThemeDark);
        tvThemeSystem = findViewById(R.id.tvThemeSystem);
        
        // اللغة
        llLanguage = findViewById(R.id.llLanguage);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        
        switchDailyReminders = findViewById(R.id.switchDailyReminders);

        // تسجيل الخروج
        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            prefs.edit().remove("fullName").remove("email").remove("password").remove("isGuest").apply();
            getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().clear().apply();

            Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Sign_in.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadSettings() {
        // بنحمل الإعدادات المحفوظة
        float sensitivity = prefs.getFloat("sensorSensitivity", 50.0f);
        sliderSensitivity.setValue(sensitivity);
        updateSensitivityUI(sensitivity);

        switchFlipVibration.setChecked(prefs.getBoolean("flipVibrationSoundEnabled", true));
        switchAutoStartBreaks.setChecked(prefs.getBoolean("autoStartBreaks", false));
        switchDailyReminders.setChecked(prefs.getBoolean("dailyReminders", true));

        String currentSound = prefs.getString("ambientSoundName", "Lofi Rain");
        int selection = Arrays.asList(soundOptions).indexOf(currentSound);
        if (selection != -1) spinnerAmbientSound.setSelection(selection);

        int theme = prefs.getInt("themeMode", 0); 
        updateThemeUI(theme);

        String lang = LanguageHelper.getLanguage(this);
        tvCurrentLanguage.setText(lang.equals("ar") ? R.string.arabic : R.string.english);
    }

    private void setupListeners() {
        // تغيير الحساسية
        sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            updateSensitivityUI(value);
            prefs.edit().putFloat("sensorSensitivity", value).apply();
        });

        // تشغيل/إيقاف الخيارات
        switchFlipVibration.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("flipVibrationSoundEnabled", isChecked).apply());
        
        switchAutoStartBreaks.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("autoStartBreaks", isChecked).apply());
        
        switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("dailyReminders", isChecked).apply());

        // اختيار صوت المحيط
        spinnerAmbientSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putString("ambientSoundName", soundOptions[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // تغيير المظهر
        tvThemeLight.setOnClickListener(v -> changeTheme(0));
        tvThemeDark.setOnClickListener(v -> changeTheme(1));
        tvThemeSystem.setOnClickListener(v -> changeTheme(2));

        // تغيير اللغة
        llLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        // ديالوج اختيار اللغة
        String[] languages = {getString(R.string.english), getString(R.string.arabic)};
        String[] codes = {"en", "ar"};
        
        String currentLang = LanguageHelper.getLanguage(this);
        int checkedItem = currentLang.equals("ar") ? 1 : 0;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.app_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    LanguageHelper.setLocale(settings.this, codes[which]);
                    dialog.dismiss();
                    recreate(); 
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateSensitivityUI(float value) {
        // تحديث النص واللون للحساسية
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
        // تطبيق المظهر الجديد
        prefs.edit().putInt("themeMode", mode).apply();
        updateThemeUI(mode);
        
        if (mode == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (mode == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private void updateThemeUI(int mode) {
        // تحديث شكل أزرار المظهر
        tvThemeLight.setBackgroundResource(0);
        tvThemeLight.setTextColor(getResources().getColor(R.color.text_gray));
        tvThemeDark.setBackgroundResource(0);
        tvThemeDark.setTextColor(getResources().getColor(R.color.text_gray));
        tvThemeSystem.setBackgroundResource(0);
        tvThemeSystem.setTextColor(getResources().getColor(R.color.text_gray));

        TextView selected = mode == 0 ? tvThemeLight : (mode == 1 ? tvThemeDark : tvThemeSystem);
        selected.setBackgroundResource(R.color.white);
        selected.setTextColor(getResources().getColor(R.color.black));
    }
}
