package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import android.widget.NumberPicker;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.graphics.Bitmap;

public class Edit_profile extends AppCompatActivity {

    private ShapeableImageView ivProfile;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveImageLocally(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    saveBitmapLocally(photo);
                }
            }
    );

    private void saveImageLocally(Uri uri) {
        try {
            java.io.File file = new java.io.File(getFilesDir(), "profile_image.jpg");
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("profileImageUri", file.getAbsolutePath()).apply();
            loadProfileImage(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBitmapLocally(Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(getFilesDir(), "profile_image.jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("profileImageUri", file.getAbsolutePath()).apply();
            loadProfileImage(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImage(String path) {
        if (path != null) {
            Glide.with(this)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivProfile);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(intent);
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etAcademicMajor = findViewById(R.id.etAcademicMajor);
        EditText etDailyFocus = findViewById(R.id.etDailyFocus);
        ivProfile = findViewById(R.id.ivProfile);
        ImageView ivCameraIcon = findViewById(R.id.ivCameraIcon);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        TextView tvCancel = findViewById(R.id.tvCancel);
        ImageView ivBack = findViewById(R.id.ivBack);
        Slider sliderSensitivity = findViewById(R.id.sliderSensitivity);
        TextView tvSensitivityLevel = findViewById(R.id.tvSensitivityLevel);

        // Load existing data
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        etFullName.setText(prefs.getString("fullName", "Alex Rivera"));
        etAcademicMajor.setText(prefs.getString("academicMajor", "Enter your major or role"));
        etDailyFocus.setText(prefs.getString("dailyFocusGoal", "3 Hours"));
        
        float savedSensitivity = prefs.getFloat("sensorSensitivity", 50.0f);
        sliderSensitivity.setValue(savedSensitivity);
        updateSensitivityUI(tvSensitivityLevel, savedSensitivity);

        // Slider Listener
        sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            updateSensitivityUI(tvSensitivityLevel, value);
        });

        String imageUriStr = prefs.getString("profileImageUri", null);
        if (imageUriStr != null) {
            loadProfileImage(imageUriStr);
        }

        // Image Selection Logic
        View.OnClickListener imageClickListener = v -> showImageSourceDialog();
        ivProfile.setOnClickListener(imageClickListener);
        if (ivCameraIcon != null) {
            ivCameraIcon.setOnClickListener(imageClickListener);
        }

        // Goal Picker Logic
        View.OnClickListener goalClickListener = v -> showSetFocusGoalDialog(etDailyFocus);
        etDailyFocus.setOnClickListener(goalClickListener);
        findViewById(R.id.ivGoalIcon).setOnClickListener(goalClickListener);

        // Save Button Logic
        btnSave.setOnClickListener(v -> {
            String newName = etFullName.getText().toString().trim();
            String newMajor = etAcademicMajor.getText().toString().trim();
            String newGoal = etDailyFocus.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fullName", newName);
                editor.putString("academicMajor", newMajor);
                editor.putString("dailyFocusGoal", newGoal);
                editor.putFloat("sensorSensitivity", sliderSensitivity.getValue());
                editor.apply();

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Cancel and Back logic
        tvCancel.setOnClickListener(v -> finish());
        ivBack.setOnClickListener(v -> finish());
    }

    private void showSetFocusGoalDialog(EditText targetEditText) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_focus_h_goal, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        NumberPicker npHours = dialogView.findViewById(R.id.npHours);
        NumberPicker npMinutes = dialogView.findViewById(R.id.npMinutes);

        // Configure Hours
        npHours.setMinValue(0);
        npHours.setMaxValue(24);
        npHours.setValue(3);

        // Configure Minutes
        String[] minutesValues = {"00", "15", "30", "45"};
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(minutesValues.length - 1);
        npMinutes.setDisplayedValues(minutesValues);
        npMinutes.setValue(0);

        dialogView.findViewById(R.id.btnSaveFocusGoal).setOnClickListener(v -> {
            int h = npHours.getValue();
            String m = minutesValues[npMinutes.getValue()];
            String result = h + " Hours " + m + " Minutes";
            if (h == 0 && m.equals("00")) {
                Toast.makeText(this, "Please set a goal higher than 0", Toast.LENGTH_SHORT).show();
            } else {
                targetEditText.setText(result);
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancelFocusGoal).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateSensitivityUI(TextView tv, float value) {
        if (value == 0) {
            tv.setText(R.string.low);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_red));
            tv.setBackgroundResource(R.drawable.badge_bg_red);
        } else if (value == 50) {
            tv.setText(R.string.medium);
            tv.setTextColor(android.graphics.Color.parseColor("#B8860B"));
            tv.setBackgroundResource(R.drawable.badge_bg_yellow);
        } else {
            tv.setText(R.string.high);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_green));
            tv.setBackgroundResource(R.drawable.badge_bg_green);
        }
    }
}