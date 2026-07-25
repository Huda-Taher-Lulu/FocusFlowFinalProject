package com.example.finalproject_hudalulu_220253814;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.app.NotificationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class Smart_timer extends AppCompatActivity implements SensorEventListener {

    private TextView tvTimerDisplay, tvTaskTitleDisplay, tvTaskCategoryDisplay, tvFlipTitle, tvFlipDesc;
    private ProgressBar progressBar;
    private ImageView ivTaskIcon, ivSelectTask, ivIllustration;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isPaused = false;
    private boolean isAwaitingSave = false;
    private long startTimeInMillis = 0;
    private long timeLeftInMillis = 0;
    private String selectedAudio = "Silent";
    private long taskId = -1;
    private String currentTaskTitle = "";
    private MediaPlayer bgMediaPlayer;
    private boolean isDndEnabled = false;

    private LinearLayout llLofiOption, llSilentOption, llDndOption, llNoTaskReminder;
    private TextView tvLofi, tvSilent, tvDnd;
    private ImageView ivDndIcon;
    private LinearLayout llChoiceButtons;
    private float sensorThreshold = -8.0f; // Default Medium
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_timer);

        // Keep screen on during the focus session
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Settings / Profile
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Smart_timer.this, settings.class));
        });

        ShapeableImageView ivProfileNav = findViewById(R.id.ivProfile);
        if (ivProfileNav != null) {
            ivProfileNav.setOnClickListener(v -> {
                startActivity(new Intent(Smart_timer.this, Profile.class));
            });

            // Load Profile Image
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imagePath = prefs.getString("profileImageUri", null);
            if (imagePath != null) {
                Glide.with(this)
                        .load(imagePath)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivProfileNav);
            }
        }


        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        tvTaskTitleDisplay = findViewById(R.id.tvTaskTitleDisplay);
        tvTaskCategoryDisplay = findViewById(R.id.tvTaskCategoryDisplay);
        tvFlipTitle = findViewById(R.id.tvFlipTitle);
        tvFlipDesc = findViewById(R.id.tvFlipDesc);
        ivTaskIcon = findViewById(R.id.ivTaskIcon);
        ivSelectTask = findViewById(R.id.ivSelectTask);
        ivIllustration = findViewById(R.id.ivIllustration);
        progressBar = findViewById(R.id.progressBar);
        llNoTaskReminder = findViewById(R.id.llNoTaskReminder);

        findViewById(R.id.llTimerClickArea).setOnClickListener(v -> showDurationPickerDialog());

        View.OnClickListener toTaskManager = v -> {
            startActivity(new Intent(this, Task_manager.class));
        };
        ivSelectTask.setOnClickListener(toTaskManager);
        findViewById(R.id.cvActiveTask).setOnClickListener(toTaskManager);

        llChoiceButtons = findViewById(R.id.llChoiceButtons);
        llLofiOption = findViewById(R.id.llLofiOption);
        llSilentOption = findViewById(R.id.llSilentOption);
        llDndOption = findViewById(R.id.llDndOption);
        tvLofi = findViewById(R.id.tvLofi);
        tvSilent = findViewById(R.id.tvSilent);
        tvDnd = findViewById(R.id.tvDnd);
        ivDndIcon = findViewById(R.id.ivDndIcon);

        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navTasks.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Task_manager.class)));
        navTimer.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Smart_timer.class)));
        navDashboard.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Dashboard.class)));
        navStats.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Focus_state.class)));

        llLofiOption.setOnClickListener(v -> updateAudioUI("Lofi Beats"));
        llSilentOption.setOnClickListener(v -> updateAudioUI("Silent"));
        llDndOption.setOnClickListener(v -> toggleDnd());

        // Load duration and preferences from intent or SharedPreferences
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DURATION")) {
            // Start a NEW task session or resume from repository
            taskId = intent.getLongExtra("TASK_ID", -1);
            if (taskId == -1) {
                com.example.finalproject_hudalulu_220253814.models.Task t =
                        (com.example.finalproject_hudalulu_220253814.models.Task) intent.getSerializableExtra("TASK");
                if (t != null) taskId = t.getId();
            }

            // Always fetch the freshest task data from repository
            com.example.finalproject_hudalulu_220253814.models.Task repoTask =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);

            if (repoTask != null) {
                startTimeInMillis = (long) repoTask.getDuration() * 60000;
                if (repoTask.getTimeLeftInMillis() > 0 && repoTask.getTimeLeftInMillis() < startTimeInMillis) {
                    timeLeftInMillis = repoTask.getTimeLeftInMillis();
                } else {
                    timeLeftInMillis = startTimeInMillis;
                }
                currentTaskTitle = repoTask.getTitle();
            } else {
                int durationMins = intent.getIntExtra("DURATION", 25);
                startTimeInMillis = durationMins * 60 * 1000L;
                timeLeftInMillis = startTimeInMillis;
                currentTaskTitle = intent.getStringExtra("TASK_TITLE");
            }

            selectedAudio = intent.getStringExtra("TASK_AUDIO");

            updateAudioUI(selectedAudio);
            updateDndUI();
            setupTaskUI();
        } else {
            // Try to load an EXISTING active session
            if (!loadActiveSession()) {
                // No task selected and no active session
                startTimeInMillis = 0;
                timeLeftInMillis = 0;
                tvFlipTitle.setText("No Task Selected");
                tvTaskTitleDisplay.setText("No Task Selected");
                tvTaskCategoryDisplay.setText("None");
                if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.VISIBLE);
            }
        }

        updateCountDownText();
        setupProgressBar();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateAudioUI(String pref) {
        selectedAudio = pref;
        if ("Lofi Beats".equalsIgnoreCase(pref)) {
            llLofiOption.setBackgroundResource(R.drawable.chip_selected);
            tvLofi.setTextColor(Color.WHITE);
            llSilentOption.setBackgroundResource(R.drawable.toggle_bg);
            tvSilent.setTextColor(Color.BLACK);
            if (isTimerRunning) {
                startBackgroundMusic();
            }
        } else {
            llSilentOption.setBackgroundResource(R.drawable.chip_selected);
            tvSilent.setTextColor(Color.WHITE);
            llLofiOption.setBackgroundResource(R.drawable.toggle_bg);
            tvLofi.setTextColor(Color.BLACK);
            pauseBackgroundMusic();
        }
    }

    private void toggleDnd() {
        isDndEnabled = !isDndEnabled;
        if (isDndEnabled) {
            checkNotificationPolicyAccess();
        }
        updateDndUI();
        if (isTimerRunning) {
            setDoNotDisturb(isDndEnabled);
        }
    }

    private void updateDndUI() {
        if (isDndEnabled) {
            llDndOption.setBackgroundResource(R.drawable.chip_selected);
            tvDnd.setTextColor(Color.WHITE);
            ivDndIcon.setColorFilter(Color.WHITE);
        } else {
            llDndOption.setBackgroundResource(R.drawable.toggle_bg);
            tvDnd.setTextColor(Color.BLACK);
            ivDndIcon.setColorFilter(Color.BLACK);
        }
    }

    private void startBackgroundMusic() {
        if ("Lofi Beats".equalsIgnoreCase(selectedAudio)) {
            if (bgMediaPlayer == null) {
                bgMediaPlayer = MediaPlayer.create(this, R.raw.calm);
                bgMediaPlayer.setLooping(true);
            }
            if (!bgMediaPlayer.isPlaying()) {
                bgMediaPlayer.start();
            }
        }
    }

    private void pauseBackgroundMusic() {
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.pause();
        }
    }

    private void stopBackgroundMusic() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundMusic();
        setDoNotDisturb(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSensorSensitivity();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (isTimerRunning) {
            pauseTimer();
        }
        saveActiveSession();
    }

    private void setupTaskUI() {
        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                tvTaskTitleDisplay.setText(task.getTitle());
                tvTaskCategoryDisplay.setText(task.getCategory());

                // Set category icon
                if ("Study".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_book);
                } else if ("Code".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_code);
                } else if ("Design".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_palette);
                }
            }
        }
        tvFlipDesc.setVisibility(View.GONE);
        llChoiceButtons.setVisibility(View.VISIBLE);
        if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.GONE);
    }

    private void showDurationPickerDialog() {
        if (isTimerRunning) {
            Toast.makeText(this, "Finish or pause the timer to edit duration", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_timer, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etHours = dialogView.findViewById(R.id.etHours);
        EditText etMinutes = dialogView.findViewById(R.id.etMinutes);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveGoal);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelGoal);

        int currentMins = (int) (startTimeInMillis / 60000);
        etHours.setText(String.valueOf(currentMins / 60));
        etMinutes.setText(String.valueOf(currentMins % 60));

        btnSave.setOnClickListener(v -> {
            String h = etHours.getText().toString().trim();
            String m = etMinutes.getText().toString().trim();

            try {
                int hours = Integer.parseInt(h.isEmpty() ? "0" : h);
                int minutes = Integer.parseInt(m.isEmpty() ? "0" : m);
                int totalMins = (hours * 60) + minutes;

                if (totalMins > 0) {
                    startTimeInMillis = totalMins * 60000L;
                    timeLeftInMillis = startTimeInMillis;

                    // Update the task duration in the repository if a task is selected
                    if (taskId != -1) {
                        com.example.finalproject_hudalulu_220253814.models.Task task =
                                com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
                        if (task != null) {
                            task.setDuration(totalMins);
                            task.setTimeLeftInMillis(startTimeInMillis);
                            com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).updateTask(this, task);
                        }
                    }

                    updateCountDownText();
                    setupProgressBar();
                    saveActiveSession();
                    dialog.dismiss();
                    Toast.makeText(this, "Timer updated and saved to task!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter a valid duration", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupProgressBar() {
        progressBar.setMax(startTimeInMillis > 0 ? (int) (startTimeInMillis / 1000) : 100);
        progressBar.setProgress(timeLeftInMillis > 0 ? (int) (timeLeftInMillis / 1000) : 0);
    }

    private void saveActiveSession() {
        // Save to TimerPrefs for quick restoration
        android.content.SharedPreferences prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startTimeInMillis", startTimeInMillis);
        editor.putLong("timeLeftInMillis", timeLeftInMillis);
        editor.putBoolean("isTimerRunning", isTimerRunning);
        editor.putBoolean("isPaused", isPaused);
        editor.putString("currentTaskTitle", currentTaskTitle);
        editor.putString("selectedAudio", selectedAudio);
        editor.putBoolean("isDndEnabled", isDndEnabled);
        editor.putLong("taskId", taskId);
        editor.putBoolean("isAwaitingSave", isAwaitingSave);
        editor.putBoolean("hasActiveSession", startTimeInMillis > 0 && !isAwaitingSave);
        editor.apply();

        // Also save specific progress back to the Task object in repository
        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                task.setTimeLeftInMillis(timeLeftInMillis);
                com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).updateTask(this, task);
            }
        }
    }

    private boolean loadActiveSession() {
        android.content.SharedPreferences prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("hasActiveSession", false)) return false;

        startTimeInMillis = prefs.getLong("startTimeInMillis", 0);
        timeLeftInMillis = prefs.getLong("timeLeftInMillis", 0);
        isTimerRunning = false;
        isPaused = prefs.getBoolean("isPaused", true) || prefs.getBoolean("isTimerRunning", false);
        currentTaskTitle = prefs.getString("currentTaskTitle", "");
        selectedAudio = prefs.getString("selectedAudio", "Silent");
        isDndEnabled = prefs.getBoolean("isDndEnabled", false);
        taskId = prefs.getLong("taskId", -1);
        isAwaitingSave = prefs.getBoolean("isAwaitingSave", false);

        updateAudioUI(selectedAudio);
        updateDndUI();
        setupTaskUI();
        if (isPaused) {
        }
        return true;
    }

    private void clearActiveSession() {
        getSharedPreferences("TimerPrefs", MODE_PRIVATE).edit().clear().apply();
    }

    private void loadSensorSensitivity() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        float sensitivityValue = prefs.getFloat("sensorSensitivity", 50.0f);

        if (sensitivityValue == 0) {
            sensorThreshold = -9.5f;
        } else if (sensitivityValue == 50) {
            sensorThreshold = -8.0f;
        } else {
            sensorThreshold = -6.0f;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];

            if (z < sensorThreshold) { // Face down
                if (!isTimerRunning && !isAwaitingSave && startTimeInMillis > 0 && timeLeftInMillis > 0) {
                    if (isPaused) {
                        resumeTimer();
                    } else {
                        startTimer();
                    }
                }
            } else if (z > 5.0) { // Face up
                if (isTimerRunning) {
                    pauseTimer();
                    playSound(R.raw.warning_sound);
                    Toast.makeText(this, "Don't lose focus! Flip back to continue.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void startTimer() {
        if (isDndEnabled && !checkNotificationPolicyAccess()) {
            return;
        }
        isTimerRunning = true;
        isPaused = false;
        playSound(R.raw.start_sound);
        startBackgroundMusic();

        if (isDndEnabled) {
            setDoNotDisturb(true);
        }

        // Update task status to IN PROGRESS only when started
        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                task.setStatus("IN PROGRESS");
                com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).updateTask(this, task);
            }
        }

        enterDeepFlow();
        runTimer();
    }

    private void resumeTimer() {
        if (isDndEnabled && !checkNotificationPolicyAccess()) {
            return;
        }
        isTimerRunning = true;
        isPaused = false;
        playSound(R.raw.resume_sound);
        startBackgroundMusic();

        if (isDndEnabled) {
            setDoNotDisturb(true);
        }

        enterDeepFlow();
        runTimer();
    }

    private void pauseTimer() {
        isTimerRunning = false;
        isPaused = true;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        pauseBackgroundMusic();
        setDoNotDisturb(false);

        // Show edit controls again
        ivSelectTask.setVisibility(View.VISIBLE);
        findViewById(R.id.llTimerClickArea).setClickable(true);
        tvFlipTitle.setText("Focus Paused!");
        ivIllustration.setAlpha(1.0f);
    }

    private boolean checkNotificationPolicyAccess() {
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, "Please grant DND access to silence notifications", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private void setDoNotDisturb(boolean active) {
        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            if (active) {
                // Use PRIORITY filter instead of NONE to allow Media (Music) to play
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
    }

    private void runTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                if (isAwaitingSave) return; // Prevent multiple calls

                isTimerRunning = false;
                isPaused = false;
                isAwaitingSave = true; // Prevent restart loop
                timeLeftInMillis = 0;
                updateCountDownText();
                updateProgressBar();

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }

                playSound(R.raw.success_sound);
                stopBackgroundMusic();
                setDoNotDisturb(false);

                showSessionCompletedDialog();
                clearActiveSession();
            }
        }.start();
    }

    private void clearTimerAndUI() {
        startTimeInMillis = 0;
        timeLeftInMillis = 0;
        taskId = -1;
        currentTaskTitle = "";
        isAwaitingSave = false;

        updateCountDownText();
        updateProgressBar();

        tvFlipTitle.setText("No Task Selected");
        tvTaskTitleDisplay.setText("No Task Selected");
        tvTaskCategoryDisplay.setText("None");
        llChoiceButtons.setVisibility(View.VISIBLE); // Keep buttons visible but they won't do much
        if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.VISIBLE);
        ivIllustration.setAlpha(1.0f);
    }

    private void showSessionCompletedDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_session_completed, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvSessionTime = dialogView.findViewById(R.id.tvSessionTime);
        EditText etReflectionInput = dialogView.findViewById(R.id.etReflectionInput);
        int totalMinutes = (int) (startTimeInMillis / 60000);
        tvSessionTime.setText(totalMinutes + " mins");

        dialogView.findViewById(R.id.btnSaveFinish).setOnClickListener(v -> {
            String notes = etReflectionInput != null ? etReflectionInput.getText().toString().trim() : "";
            updateRealStats(notes);
            dialog.dismiss();

            clearTimerAndUI();
            finish();
        });

        dialogView.findViewById(R.id.btnDismiss).setOnClickListener(v -> {
            dialog.dismiss();
            clearTimerAndUI();
        });

        dialogView.findViewById(R.id.ivClose).setOnClickListener(v -> {
            dialog.dismiss();
            clearTimerAndUI();
        });

        dialog.show();
    }

    private void updateRealStats(String notes) {
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        // 1. Update Focus Minutes
        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        // Calculate actually spent minutes (startTime - timeLeft)
        long sessionDurationMinutes = (startTimeInMillis - timeLeftInMillis) / 60000;

        // If session finished naturally, it might be exactly startTimeInMillis
        if (sessionDurationMinutes <= 0) {
            sessionDurationMinutes = startTimeInMillis / 60000;
        }

        editor.putLong("totalFocusMinutes", totalMinutes + sessionDurationMinutes);

        // 2. Update Completed Tasks Count (Global Stat)
        int tasksCount = prefs.getInt("completedTasksCount", 0);
        editor.putInt("completedTasksCount", tasksCount + 1);

        // 3. Update Specific Task Status in Repository
        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setTimeLeftInMillis(0); // Mark as fully done
                task.setNotes(notes);
                task.setCompletionTimestamp(System.currentTimeMillis());
                com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).updateTask(this, task);
            }
        }

        // Update Streak
        int currentStreak = prefs.getInt("currentStreak", 0);
        long lastTimestamp = prefs.getLong("lastActiveTimestamp", 0);

        java.util.Calendar cal = java.util.Calendar.getInstance();

        // Today at midnight
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        // Yesterday at midnight
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        long yesterdayStart = cal.getTimeInMillis();

        if (lastTimestamp >= todayStart) {
            // Already updated today, do nothing to streak
        } else if (lastTimestamp >= yesterdayStart) {
            // Continued from yesterday
            editor.putInt("currentStreak", currentStreak + 1);
        } else {
            // New streak
            editor.putInt("currentStreak", 1);
        }

        editor.putLong("lastActiveTimestamp", System.currentTimeMillis());
        editor.apply();

        // Clear active session since it's finished
        clearActiveSession();

        Toast.makeText(this, "Stats Updated!", Toast.LENGTH_SHORT).show();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void updateProgressBar() {
        progressBar.setProgress((int) (timeLeftInMillis / 1000));
    }

    private void enterDeepFlow() {
        ivSelectTask.setVisibility(View.GONE);
        findViewById(R.id.llTimerClickArea).setClickable(false);
        tvFlipTitle.setText("Deep Flow Active");
        ivIllustration.setAlpha(0.3f);
    }

    private void playSound(int resId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("flipVibrationSoundEnabled", true);

        if (!soundEnabled) return;

        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) {
                // Set volume to 30% (lower than the default 100%)
                mp.setVolume(0.3f, 0.3f);
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
