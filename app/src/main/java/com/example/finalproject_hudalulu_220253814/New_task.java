package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject_hudalulu_220253814.models.Task;
import com.example.finalproject_hudalulu_220253814.models.TaskRepository;
import com.google.android.material.slider.Slider;

public class New_task extends AppCompatActivity {

    private static final String KEY_TITLE = "draftTitle";
    private static final String KEY_DURATION = "draftDuration";
    private static final String KEY_CATEGORY = "draftCategory";
    private static final String KEY_AUDIO = "draftAudio";

    private String selectedCategory = "Study";
    private String selectedAudio = "Silent";

    private EditText etTaskTitle;
    private Slider sliderDuration;
    private TextView tvDurationValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTaskTitle = findViewById(R.id.etTaskTitle);
        sliderDuration = findViewById(R.id.sliderDuration);
        tvDurationValue = findViewById(R.id.tvDurationValue);

        sliderDuration.addOnChangeListener((slider, value, fromUser) -> {
            tvDurationValue.setText((int) value + "m");
        });

        // Category Selection
        LinearLayout btnStudy = findViewById(R.id.btnCategoryStudy);
        LinearLayout btnCode = findViewById(R.id.btnCategoryCode);
        LinearLayout btnDesign = findViewById(R.id.btnCategoryDesign);

        btnStudy.setOnClickListener(v -> {
            selectedCategory = "Study";
            updateCategoryUI();
        });
        btnCode.setOnClickListener(v -> {
            selectedCategory = "Code";
            updateCategoryUI();
        });
        btnDesign.setOnClickListener(v -> {
            selectedCategory = "Design";
            updateCategoryUI();
        });

        findViewById(R.id.ivAddCategory).setOnClickListener(v -> showAddCategoryDialog());

        // Load existing custom categories
        for (String cat : TaskRepository.getInstance(this).getCustomCategories()) {
            addNewCategoryButton(cat, false);
        }

        // Audio Preference Selection
        TextView btnLofi = findViewById(R.id.btnAudioLofi);
        TextView btnSilent = findViewById(R.id.btnAudioSilent);

        btnLofi.setOnClickListener(v -> {
            selectedAudio = "Lofi Beats";
            btnLofi.setBackgroundResource(R.drawable.chip_selected);
            btnLofi.setTextColor(getResources().getColor(R.color.white));
            btnSilent.setBackground(null);
            btnSilent.setTextColor(getResources().getColor(R.color.text_gray));
        });

        btnSilent.setOnClickListener(v -> {
            selectedAudio = "Silent";
            btnSilent.setBackgroundResource(R.drawable.chip_selected);
            btnSilent.setTextColor(getResources().getColor(R.color.white));
            btnLofi.setBackground(null);
            btnLofi.setTextColor(getResources().getColor(R.color.text_gray));
        });

        if (savedInstanceState != null) {
            etTaskTitle.setText(savedInstanceState.getString(KEY_TITLE, ""));
            sliderDuration.setValue(savedInstanceState.getFloat(KEY_DURATION, sliderDuration.getValue()));
            tvDurationValue.setText((int) sliderDuration.getValue() + "m");
            selectedCategory = savedInstanceState.getString(KEY_CATEGORY, selectedCategory);
            selectedAudio = savedInstanceState.getString(KEY_AUDIO, selectedAudio);
            if ("Lofi Beats".equals(selectedAudio)) {
                btnLofi.performClick();
            } else {
                btnSilent.performClick();
            }
        }

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSaveTask).setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.error_task_title_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            int duration = (int) sliderDuration.getValue();

            Task newTask = new Task(title, duration, selectedCategory, selectedAudio);
            TaskRepository.getInstance(this).addTask(this, newTask);

            Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
            finish();
        });

        // Navigation Bar
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> startActivity(new Intent(this, Dashboard.class)));
        }
        if (navTimer != null) {
            navTimer.setOnClickListener(v -> startActivity(new Intent(this, Smart_timer.class)));
        }
        if (navStats != null) {
            navStats.setOnClickListener(v -> startActivity(new Intent(this, Focus_state.class)));
        }

        // Initial UI update
        updateCategoryUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, etTaskTitle.getText().toString());
        outState.putFloat(KEY_DURATION, sliderDuration.getValue());
        outState.putString(KEY_CATEGORY, selectedCategory);
        outState.putString(KEY_AUDIO, selectedAudio);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedCategory = savedInstanceState.getString(KEY_CATEGORY, selectedCategory);
        selectedAudio = savedInstanceState.getString(KEY_AUDIO, selectedAudio);
        updateCategoryUI();
    }

    private void updateCategoryUI() {
        LinearLayout container = findViewById(R.id.llCategoriesContainer);
        if (container == null) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            android.view.View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                String tag = (child.getTag() != null) ? child.getTag().toString() : "";
                if (child.getId() == R.id.btnCategoryStudy) tag = "Study";
                else if (child.getId() == R.id.btnCategoryCode) tag = "Code";
                else if (child.getId() == R.id.btnCategoryDesign) tag = "Design";

                boolean isSelected = selectedCategory.equalsIgnoreCase(tag);

                // Apply visual changes for selected state
                child.setAlpha(isSelected ? 1.0f : 0.4f);
                child.setScaleX(isSelected ? 1.05f : 1.0f);
                child.setScaleY(isSelected ? 1.05f : 1.0f);

                // Elevation effect
                child.setElevation(isSelected ? 8f : 0f);
            }
        }
    }

    private void showAddCategoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCategoryInput = dialogView.findViewById(R.id.etCategoryInput);
        View btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);
        View btnCancelCategory = dialogView.findViewById(R.id.btnCancelAddition);

        btnAddCategory.setOnClickListener(v -> {
            String categoryName = etCategoryInput.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                TaskRepository.getInstance(this).addCategory(this, categoryName);
                addNewCategoryButton(categoryName, true);
            }
            dialog.dismiss();
        });

        btnCancelCategory.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addNewCategoryButton(String name, boolean autoSelect) {
        LinearLayout container = findViewById(R.id.llCategoriesContainer);

        LinearLayout newBtn = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (44 * getResources().getDisplayMetrics().density));
        lp.setMarginStart((int) (12 * getResources().getDisplayMetrics().density));
        newBtn.setLayoutParams(lp);
        newBtn.setBackgroundResource(R.drawable.edit_text_bg);
        newBtn.setGravity(android.view.Gravity.CENTER);
        newBtn.setPadding((int) (16 * getResources().getDisplayMetrics().density), 0,
                (int) (16 * getResources().getDisplayMetrics().density), 0);
        newBtn.setTag(name);

        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setTextColor(getResources().getColor(R.color.text_gray));
        tv.setTypeface(ResourcesCompat.getFont(this, R.font.quicksand), android.graphics.Typeface.BOLD);

        newBtn.addView(tv);
        newBtn.setOnClickListener(v -> {
            selectedCategory = name;
            updateCategoryUI();
        });

        // Add before the "+" button
        container.addView(newBtn, container.getChildCount() - 1);

        if (autoSelect) {
            // Auto select the new category
            selectedCategory = name;
            updateCategoryUI();
        }
    }
}
