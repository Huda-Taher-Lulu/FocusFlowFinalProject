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
import android.graphics.Bitmap;

public class Edit_profile extends AppCompatActivity {

    private ShapeableImageView ivProfile;

    // تشغيل الجاليري
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) saveImageLocally(uri);
            }
    );

    // تشغيل الكاميرا
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
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("profileImageUri", file.getAbsolutePath()).apply();
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
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("profileImageUri", file.getAbsolutePath()).apply();
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
        // نختار بين الكاميرا أو الاستوديو
        String[] options = {getString(R.string.camera), getString(R.string.gallery)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_image_source)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
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

        // ربط العناصر مع الـ View
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etAcademicMajor = findViewById(R.id.etAcademicMajor);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etAddress = findViewById(R.id.etAddress);
        EditText etDailyFocus = findViewById(R.id.etDailyFocus);
        ivProfile = findViewById(R.id.ivProfile);
        ImageView ivCameraIcon = findViewById(R.id.ivCameraIcon);
        Slider sliderSensitivity = findViewById(R.id.sliderSensitivity);
        TextView tvSensitivityLevel = findViewById(R.id.tvSensitivityLevel);

        // تحميل البيانات الحالية
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        etFullName.setText(prefs.getString("fullName", ""));
        etAcademicMajor.setText(prefs.getString("academicMajor", ""));
        etEmail.setText(prefs.getString("email", ""));
        etPhone.setText(prefs.getString("phone", ""));
        etAddress.setText(prefs.getString("address", ""));
        etDailyFocus.setText(prefs.getString("dailyFocusGoal", "3 Hours"));
        
        float savedSensitivity = prefs.getFloat("sensorSensitivity", 50.0f);
        sliderSensitivity.setValue(savedSensitivity);
        updateSensitivityUI(tvSensitivityLevel, savedSensitivity);

        sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> updateSensitivityUI(tvSensitivityLevel, value));

        String imageUriStr = prefs.getString("profileImageUri", null);
        if (imageUriStr != null) loadProfileImage(imageUriStr);

        // تغيير الصورة
        View.OnClickListener imageClickListener = v -> showImageSourceDialog();
        ivProfile.setOnClickListener(imageClickListener);
        if (ivCameraIcon != null) ivCameraIcon.setOnClickListener(imageClickListener);

        // تعديل الهدف اليومي
        View.OnClickListener goalClickListener = v -> showSetFocusGoalDialog(etDailyFocus);
        etDailyFocus.setOnClickListener(goalClickListener);
        findViewById(R.id.ivGoalIcon).setOnClickListener(goalClickListener);

        // حفظ التعديلات
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String newName = etFullName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "رجاءً عبي الحقول المطلوبة", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("fullName", newName);
            editor.putString("academicMajor", etAcademicMajor.getText().toString().trim());
            editor.putString("email", newEmail);
            editor.putString("phone", newPhone);
            editor.putString("address", newAddress);
            editor.putString("dailyFocusGoal", etDailyFocus.getText().toString().trim());
            editor.putFloat("sensorSensitivity", sliderSensitivity.getValue());
            editor.apply();

            Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // تغيير كلمة المرور
        View cvChangePassword = findViewById(R.id.cvChangePassword);
        if (cvChangePassword != null) cvChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        // ديالوج تغيير كلمة السر
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);

        dialogView.findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedPass = prefs.getString("password", "");

            if (!etCurrentPassword.getText().toString().equals(savedPass)) {
                Toast.makeText(this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                return;
            }

            String newPass = etNewPassword.getText().toString();
            if (newPass.length() < 8 || !newPass.equals(etConfirmNewPassword.getText().toString())) {
                Toast.makeText(this, "تأكد من كلمة السر الجديدة", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit().putString("password", newPass).apply();
            Toast.makeText(this, R.string.password_updated, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancelPassword).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSetFocusGoalDialog(EditText targetEditText) {
        // تحديد هدف التركيز اليومي
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_focus_h_goal, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        NumberPicker npHours = dialogView.findViewById(R.id.npHours);
        NumberPicker npMinutes = dialogView.findViewById(R.id.npMinutes);

        npHours.setMinValue(0); npHours.setMaxValue(24); npHours.setValue(3);

        String[] minutesValues = {"00", "15", "30", "45"};
        npMinutes.setMinValue(0); npMinutes.setMaxValue(minutesValues.length - 1);
        npMinutes.setDisplayedValues(minutesValues);

        dialogView.findViewById(R.id.btnSaveFocusGoal).setOnClickListener(v -> {
            int h = npHours.getValue();
            String m = minutesValues[npMinutes.getValue()];
            targetEditText.setText(getString(R.string.daily_focus_goal_format, h, Integer.parseInt(m)));
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancelFocusGoal).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSensitivityUI(TextView tv, float value) {
        // تحديث لون وشكل مستوى الحساسية
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
