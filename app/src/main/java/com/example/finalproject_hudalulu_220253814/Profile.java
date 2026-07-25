package com.example.finalproject_hudalulu_220253814;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class Profile extends AppCompatActivity {

    private TextView tvProfileName, tvProfileMajor, tvFocusHours, tvTasksDone, tvStreak, tvProfileEmail, tvProfilePhone, tvProfileAddress;
    private ShapeableImageView ivProfile;
    private static final String DEFAULT_MAJOR = "Enter your major or role";

    // لاختيار صورة من الاستوديو
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveImageLocally(uri);
                }
            }
    );

    private void saveImageLocally(Uri uri) {
        try {
            // بنحفظ الصورة في ملف داخلي عشان ما تضيع
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

            // بنحفظ المسار في الـ SharedPreferences
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ربط العناصر
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileMajor = findViewById(R.id.tvProfileMajor);
        tvFocusHours = findViewById(R.id.tvFocusHours);
        tvTasksDone = findViewById(R.id.tvTasksDone);
        tvStreak = findViewById(R.id.tvStreak);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        ivProfile = findViewById(R.id.ivProfile);
        ImageView ivCameraIcon = findViewById(R.id.ivCameraIcon);
        LinearLayout llEditMajor = findViewById(R.id.llEditMajor);
        ConstraintLayout clProfileAddress = findViewById(R.id.clProfileAddress);

        refreshProfileData();

        // لو ضغطنا على العنوان بنفتح الخرائط
        if (clProfileAddress != null) {
            clProfileAddress.setOnClickListener(v -> {
                String address = tvProfileAddress.getText().toString();
                if (!address.isEmpty()) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    try {
                        startActivity(mapIntent);
                    } catch (Exception e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
                    }
                }
            });
        }

        // تغيير الصورة
        View.OnClickListener imageClickListener = v -> galleryLauncher.launch("image/*");
        ivProfile.setOnClickListener(imageClickListener);
        if (ivCameraIcon != null) ivCameraIcon.setOnClickListener(imageClickListener);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // مشاركة البروفايل
        ImageView ivShareProfile = findViewById(R.id.ivShareProfile);
        if (ivShareProfile != null) {
            ivShareProfile.setOnClickListener(v -> {
                String summary = getString(R.string.profile_summary, 
                        tvProfileName.getText(), 
                        tvFocusHours.getText(), 
                        tvTasksDone.getText(), 
                        tvStreak.getText());
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, summary);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_profile_title)));
            });
        }

        if (llEditMajor != null) llEditMajor.setOnClickListener(v -> showEditMajorDialog());

        ConstraintLayout clEditProfile = findViewById(R.id.clEditProfile);
        if (clEditProfile != null) {
            clEditProfile.setOnClickListener(v -> startActivity(new Intent(Profile.this, Edit_profile.class)));
        }
    }

    private void refreshProfileData() {
        // بنحدث البيانات من الـ Prefs
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        tvProfileName.setText(prefs.getString("fullName", getString(R.string.profile_name_default)));
        tvProfileMajor.setText(prefs.getString("academicMajor", getString(R.string.enter_major_role)));
        tvProfileEmail.setText(prefs.getString("email", getString(R.string.email_hint)));
        tvProfilePhone.setText(prefs.getString("phone", getString(R.string.phone_number_hint)));
        tvProfileAddress.setText(prefs.getString("address", getString(R.string.address_hint)));

        // إحصائيات المستخدم
        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        int tasksDone = prefs.getInt("completedTasksCount", 0);
        int streak = prefs.getInt("currentStreak", 0);

        if (totalMinutes >= 60) {
            tvFocusHours.setText(getString(R.string.hours_suffix, totalMinutes / 60.0f));
        } else {
            tvFocusHours.setText(getString(R.string.minutes_suffix, (int) totalMinutes));
        }

        tvTasksDone.setText(getString(R.string.tasks_done_count, tasksDone));
        tvStreak.setText(getString(R.string.days_suffix, streak));

        String imageUriStr = prefs.getString("profileImageUri", null);
        if (imageUriStr != null) loadProfileImage(imageUriStr);
        else ivProfile.setImageResource(R.drawable.ic_person);
    }

    private void showEditMajorDialog() {
        // ديالوج تعديل التخصص
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_major, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etMajorInput = dialogView.findViewById(R.id.etMajorInput);
        String currentMajor = tvProfileMajor.getText().toString();
        if (!currentMajor.equals(DEFAULT_MAJOR)) etMajorInput.setText(currentMajor);

        dialogView.findViewById(R.id.btnUpdateMajor).setOnClickListener(v -> {
            String newMajor = etMajorInput.getText().toString().trim();
            if (newMajor.isEmpty()) newMajor = DEFAULT_MAJOR;

            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("academicMajor", newMajor).apply();
            tvProfileMajor.setText(newMajor);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancelMajor).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileData();
    }
}
