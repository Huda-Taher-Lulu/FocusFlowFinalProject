package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize TaskRepository with Context for persistence
        com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Reset academic major to default on every fresh app run as requested
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .remove("academicMajor")
                .apply();

        // برمجة زر Next للانتقال إلى شاشة onboarding2
        MaterialButton btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, onboarding2.class);
            startActivity(intent);
        });

        // برمجة نص Skip للانتقال المباشر إلى شاشة SignUp
        TextView tvSkip = findViewById(R.id.tvSkip);
        tvSkip.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });
    }
}