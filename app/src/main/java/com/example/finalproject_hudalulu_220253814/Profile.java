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

    private TextView tvProfileName, tvProfileMajor, tvFocusHours, tvTasksDone, tvStreak;
    private ShapeableImageView ivProfile;
    private static final String DEFAULT_MAJOR = "Enter your major or role";

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
            // Create a local file in internal storage
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

            // Save the LOCAL file path
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("profileImageUri", file.getAbsolutePath()).apply();

            // Load with Glide
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

        // Initialize Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileMajor = findViewById(R.id.tvProfileMajor);
        tvFocusHours = findViewById(R.id.tvFocusHours);
        tvTasksDone = findViewById(R.id.tvTasksDone);
        tvStreak = findViewById(R.id.tvStreak);
        ivProfile = findViewById(R.id.ivProfile);
        ImageView ivCameraIcon = findViewById(R.id.ivCameraIcon);
        LinearLayout llEditMajor = findViewById(R.id.llEditMajor);

        // Load Data
        refreshProfileData();

        // Image Selection Logic
        View.OnClickListener imageClickListener = v -> galleryLauncher.launch("image/*");
        ivProfile.setOnClickListener(imageClickListener);
        if (ivCameraIcon != null) {
            ivCameraIcon.setOnClickListener(imageClickListener);
        }

        // Back Button
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        ImageView ivShareProfile = findViewById(R.id.ivShareProfile);
        if (ivShareProfile != null) {
            ivShareProfile.setOnClickListener(v -> {
                String summary = "I'm " + tvProfileName.getText() + " on FocusFlow — "
                        + tvFocusHours.getText() + " focused, " + tvTasksDone.getText()
                        + " tasks done, " + tvStreak.getText() + " day streak!";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, summary);
                startActivity(Intent.createChooser(shareIntent, "Share profile via"));
            });
        }

        // Edit Major/Role Dialog
        if (llEditMajor != null) {
            llEditMajor.setOnClickListener(v -> showEditMajorDialog());
        }

        // Navigate to Edit Profile
        ConstraintLayout clEditProfile = findViewById(R.id.clEditProfile);
        if (clEditProfile != null) {
            clEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Profile.this, Edit_profile.class);
                startActivity(intent);
            });
        }
    }

    private void refreshProfileData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        tvProfileName.setText(prefs.getString("fullName", "Alex Rivera"));
        tvProfileMajor.setText(prefs.getString("academicMajor", DEFAULT_MAJOR));

        // Load Real Stats
        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        int tasksDone = prefs.getInt("completedTasksCount", 0);
        int streak = prefs.getInt("currentStreak", 0);

        // Format focus hours (e.g., 2.5h or 45m)
        if (totalMinutes >= 60) {
            float hours = totalMinutes / 60.0f;
            tvFocusHours.setText(String.format(java.util.Locale.getDefault(), "%.1fh", hours));
        } else {
            tvFocusHours.setText(totalMinutes + "m");
        }

        tvTasksDone.setText(String.valueOf(tasksDone));
        tvStreak.setText(streak + "d");

        String imageUriStr = prefs.getString("profileImageUri", null);
        if (imageUriStr != null) {
            loadProfileImage(imageUriStr);
        } else {
            ivProfile.setImageResource(R.drawable.ic_person);
        }
    }

    private void showEditMajorDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_major, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etMajorInput = dialogView.findViewById(R.id.etMajorInput);
        View btnUpdateMajor = dialogView.findViewById(R.id.btnUpdateMajor);
        View btnCancelMajor = dialogView.findViewById(R.id.btnCancelMajor);

        String currentMajor = tvProfileMajor.getText().toString();
        if (!currentMajor.equals(DEFAULT_MAJOR)) {
            etMajorInput.setText(currentMajor);
        }

        btnUpdateMajor.setOnClickListener(v -> {
            String newMajor = etMajorInput.getText().toString().trim();
            if (newMajor.isEmpty()) {
                newMajor = DEFAULT_MAJOR;
            }

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("academicMajor", newMajor).apply();
            tvProfileMajor.setText(newMajor);
            dialog.dismiss();
        });

        btnCancelMajor.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfileData();
    }
}
