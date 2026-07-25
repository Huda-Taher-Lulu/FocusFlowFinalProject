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
    private int savedAudioPosition = 0;
    private long taskId = -1;
    private String currentTaskTitle = "";
    private MediaPlayer bgMediaPlayer;
    private boolean isDndEnabled = false;

    private LinearLayout llLofiOption, llSilentOption, llDndOption, llNoTaskReminder;
    private TextView tvLofi, tvSilent, tvDnd;
    private ImageView ivDndIcon;
    private LinearLayout llChoiceButtons;
    private float sensorThreshold = -8.0f; 
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smart_timer);

        // عشان الشاشة تضل شغالة وما تطفي
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // زر الإعدادات
        findViewById(R.id.ivSettings).setOnClickListener(v -> {
            startActivity(new Intent(Smart_timer.this, settings.class));
        });

        // البروفايل وصورة المستخدم
        com.google.android.material.imageview.ShapeableImageView ivProfileNav = findViewById(R.id.ivProfile);
        if (ivProfileNav != null) {
            ivProfileNav.setOnClickListener(v -> {
                startActivity(new Intent(Smart_timer.this, Profile.class));
            });

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imagePath = prefs.getString("profileImageUri", null);
            if (imagePath != null) {
                com.bumptech.glide.Glide.with(this)
                        .load(imagePath)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivProfileNav);
            }
        }

        // هان بنربط كل العناصر
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

        // نضغط على التايمر عشان نغير الوقت
        findViewById(R.id.llTimerClickArea).setOnClickListener(v -> showDurationPickerDialog());

        // نروح لمدير المهام عشان نختار مهمة
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

        // شريط التنقل السفلي
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navTimer = findViewById(R.id.navTimer);
        LinearLayout navStats = findViewById(R.id.navStats);

        navTasks.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Task_manager.class)));
        navTimer.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Smart_timer.class)));
        navDashboard.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Dashboard.class)));
        navStats.setOnClickListener(v -> startActivity(new Intent(Smart_timer.this, Focus_state.class)));

        // خيارات الصوت والهدوء
        llLofiOption.setOnClickListener(v -> updateAudioUI("Lofi Beats"));
        llSilentOption.setOnClickListener(v -> updateAudioUI("Silent"));
        llDndOption.setOnClickListener(v -> toggleDnd());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DURATION")) {
            taskId = intent.getLongExtra("TASK_ID", -1);
            if (taskId == -1) {
                com.example.finalproject_hudalulu_220253814.models.Task t =
                        (com.example.finalproject_hudalulu_220253814.models.Task) intent.getSerializableExtra("TASK");
                if (t != null) taskId = t.getId();
            }

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
            // لو ما في جلسة مخزنة بنصفر كل اشي
            if (!loadActiveSession()) {
                startTimeInMillis = 0;
                timeLeftInMillis = 0;
                taskId = -1;
                currentTaskTitle = "";
                tvFlipTitle.setText(R.string.no_task_selected);
                tvTaskTitleDisplay.setText(R.string.no_task_selected);
                tvTaskCategoryDisplay.setText(R.string.none); 
                if (llNoTaskReminder != null) {
                    llNoTaskReminder.setVisibility(View.VISIBLE);
                }
                ivIllustration.setAlpha(1.0f);
            }
        }

        updateCountDownText();
        setupProgressBar();

        // تشغيل الحساسات عشان الـ flip
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateAudioUI(String pref) {
        // تحديث شكل أزرار الصوت
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
        // تفعيل وضع عدم الإزعاج
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
                if (savedAudioPosition > 0) {
                    bgMediaPlayer.seekTo(savedAudioPosition);
                }
            }
            if (!bgMediaPlayer.isPlaying()) {
                bgMediaPlayer.start();
            }
        }
    }

    private void pauseBackgroundMusic() {
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            savedAudioPosition = bgMediaPlayer.getCurrentPosition();
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
        // بنعرض بيانات المهمة المختارة
        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                tvTaskTitleDisplay.setText(task.getTitle());
                tvTaskCategoryDisplay.setText(task.getCategory());

                if ("Study".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_book);
                } else if ("Code".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_code);
                } else if ("Design".equalsIgnoreCase(task.getCategory())) {
                    ivTaskIcon.setImageResource(R.drawable.ic_palette);
                }
                
                if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.GONE);
                tvFlipDesc.setVisibility(View.GONE);
                llChoiceButtons.setVisibility(View.VISIBLE);
                return; 
            }
        }
        
        tvTaskTitleDisplay.setText(R.string.no_task_selected);
        tvTaskCategoryDisplay.setText(R.string.none);
        if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.VISIBLE);
        llChoiceButtons.setVisibility(View.VISIBLE);
    }

    private void showDurationPickerDialog() {
        if (isTimerRunning) return;

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
                    Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.error_goal_zero, Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
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
        // بنحفظ حالة الجلسة الحالية عشان لو سكرنا التطبيق ورجعنا
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

        if (bgMediaPlayer != null) {
            editor.putInt("audioPosition", bgMediaPlayer.getCurrentPosition());
        } else {
            editor.putInt("audioPosition", savedAudioPosition);
        }

        editor.apply();

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
        // بنرجع الجلسة المخزنة
        android.content.SharedPreferences prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("hasActiveSession", false)) return false;

        startTimeInMillis = prefs.getLong("startTimeInMillis", 0);
        timeLeftInMillis = prefs.getLong("timeLeftInMillis", 0);
        
        if (startTimeInMillis <= 0 || timeLeftInMillis <= 0) {
            clearActiveSession();
            return false;
        }

        isTimerRunning = false;
        isPaused = prefs.getBoolean("isPaused", true) || prefs.getBoolean("isTimerRunning", false);
        currentTaskTitle = prefs.getString("currentTaskTitle", "");
        selectedAudio = prefs.getString("selectedAudio", "Silent");
        isDndEnabled = prefs.getBoolean("isDndEnabled", false);
        taskId = prefs.getLong("taskId", -1);
        isAwaitingSave = prefs.getBoolean("isAwaitingSave", false);
        savedAudioPosition = prefs.getInt("audioPosition", 0);

        updateAudioUI(selectedAudio);
        updateDndUI();
        setupTaskUI();
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

            // لو قلبنا التلفون على وجهه
            if (z < sensorThreshold) { 
                if (!isTimerRunning && !isAwaitingSave && startTimeInMillis > 0 && timeLeftInMillis > 0) {
                    if (isPaused) {
                        resumeTimer();
                    } else {
                        startTimer();
                    }
                }
            } else if (z > 5.0) { 
                // لو مسكنا التلفون وهو شغال
                if (isTimerRunning) {
                    pauseTimer();
                    playSound(R.raw.warning_sound);
                    Toast.makeText(this, R.string.dont_lose_focus, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void startTimer() {
        if (isDndEnabled && !checkNotificationPolicyAccess()) return;
        
        isTimerRunning = true;
        isPaused = false;
        playSound(R.raw.start_sound);
        startBackgroundMusic();

        if (isDndEnabled) setDoNotDisturb(true);

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
        if (isDndEnabled && !checkNotificationPolicyAccess()) return;
        
        isTimerRunning = true;
        isPaused = false;
        playSound(R.raw.resume_sound);
        startBackgroundMusic();

        if (isDndEnabled) setDoNotDisturb(true);

        enterDeepFlow();
        runTimer();
    }

    private void pauseTimer() {
        isTimerRunning = false;
        isPaused = true;
        if (countDownTimer != null) countDownTimer.cancel();
        
        pauseBackgroundMusic();
        setDoNotDisturb(false);

        ivSelectTask.setVisibility(View.VISIBLE);
        findViewById(R.id.llTimerClickArea).setClickable(true);
        tvFlipTitle.setText(R.string.focus_paused);
        ivIllustration.setAlpha(1.0f);
    }

    private boolean checkNotificationPolicyAccess() {
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, R.string.dnd_access_needed, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private void setDoNotDisturb(boolean active) {
        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            if (active) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }
    }

    private void runTimer() {
        // الـ CountDownTimer الفعلي
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                if (isAwaitingSave) return; 

                isTimerRunning = false;
                isPaused = false;
                isAwaitingSave = true; 
                timeLeftInMillis = 0;
                updateCountDownText();
                updateProgressBar();

                if (countDownTimer != null) countDownTimer.cancel();

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

        tvFlipTitle.setText(R.string.no_task_selected);
        tvTaskTitleDisplay.setText(R.string.no_task_selected);
        tvTaskCategoryDisplay.setText(R.string.none);
        llChoiceButtons.setVisibility(View.VISIBLE); 
        if (llNoTaskReminder != null) llNoTaskReminder.setVisibility(View.VISIBLE);
        ivIllustration.setAlpha(1.0f);
    }

    private void showSessionCompletedDialog() {
        // رسالة عند انتهاء الوقت
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
        tvSessionTime.setText(getString(R.string.mins_with_value, totalMinutes, getString(R.string.min_unit)));

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
        // تحديث الإحصائيات بعد ما نخلص الجلسة
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();

        long totalMinutes = prefs.getLong("totalFocusMinutes", 0);
        long sessionDurationMinutes = (startTimeInMillis - timeLeftInMillis) / 60000;

        if (sessionDurationMinutes <= 0) sessionDurationMinutes = startTimeInMillis / 60000;

        editor.putLong("totalFocusMinutes", totalMinutes + sessionDurationMinutes);

        int tasksCount = prefs.getInt("completedTasksCount", 0);
        editor.putInt("completedTasksCount", tasksCount + 1);

        if (taskId != -1) {
            com.example.finalproject_hudalulu_220253814.models.Task task =
                    com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).getTask(taskId);
            if (task != null) {
                task.setStatus("COMPLETED");
                task.setTimeLeftInMillis(0); 
                task.setNotes(notes);
                task.setCompletionTimestamp(System.currentTimeMillis());
                com.example.finalproject_hudalulu_220253814.models.TaskRepository.getInstance(this).updateTask(this, task);
            }
        }

        // تحديث الـ Streak
        int currentStreak = prefs.getInt("currentStreak", 0);
        long lastTimestamp = prefs.getLong("lastActiveTimestamp", 0);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        long yesterdayStart = cal.getTimeInMillis();

        if (lastTimestamp < todayStart) {
            if (lastTimestamp >= yesterdayStart) {
                editor.putInt("currentStreak", currentStreak + 1);
            } else {
                editor.putInt("currentStreak", 1);
            }
        }

        editor.putLong("lastActiveTimestamp", System.currentTimeMillis());
        editor.apply();

        clearActiveSession();

        Toast.makeText(this, R.string.stats_updated, Toast.LENGTH_SHORT).show();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        if (timeLeftInMillis == 0) {
            tvTimerDisplay.setTextColor(Color.parseColor("#C62828")); 
            progressBar.setProgressDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.circular_progress_bar_red));
        } else {
            tvTimerDisplay.setTextColor(android.graphics.Color.parseColor("#1B4332")); 
            progressBar.setProgressDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.circular_progress_bar));
        }
    }

    private void updateProgressBar() {
        progressBar.setProgress((int) (timeLeftInMillis / 1000));
    }

    private void enterDeepFlow() {
        // حالة التركيز العميق
        ivSelectTask.setVisibility(View.GONE);
        findViewById(R.id.llTimerClickArea).setClickable(false);
        tvFlipTitle.setText(R.string.deep_flow_active);
        ivIllustration.setAlpha(0.3f);
    }

    private void playSound(int resId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("flipVibrationSoundEnabled", true);

        if (!soundEnabled) return;

        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) {
                mp.setVolume(0.3f, 0.3f);
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
