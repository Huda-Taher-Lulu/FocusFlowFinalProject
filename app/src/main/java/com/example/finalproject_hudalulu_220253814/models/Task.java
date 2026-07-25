package com.example.finalproject_hudalulu_220253814.models;

import java.io.Serializable;

public class Task implements Serializable {

    private long id = -1;
    private long userId = -1;

    private String title;
    private int duration;
    private String category;
    private String audioPreference;
    private String status;
    private long timeLeftInMillis;
    private String notes;
    private long completionTimestamp;

    public Task(String title, int duration, String category, String audioPreference) {
        this.title = title;
        this.duration = duration;
        this.category = category;
        this.audioPreference = audioPreference;
        this.status = "PENDING";
        this.timeLeftInMillis = (long) duration * 60 * 1000;
        this.notes = "";
        this.completionTimestamp = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAudioPreference() { return audioPreference; }
    public void setAudioPreference(String audioPreference) { this.audioPreference = audioPreference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimeLeftInMillis() { return timeLeftInMillis; }
    public void setTimeLeftInMillis(long timeLeftInMillis) { this.timeLeftInMillis = timeLeftInMillis; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getCompletionTimestamp() { return completionTimestamp; }
    public void setCompletionTimestamp(long completionTimestamp) { this.completionTimestamp = completionTimestamp; }
}
