package com.example.calendar3;

public class Event {
    private long date; // UNIXタイムスタンプ
    private String title;
    private String description;

    public Event(long date, String title, String description) {
        this.date = date;
        this.title = title;
        this.description = description;
    }

    // ゲッターとセッター
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
