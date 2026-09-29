package com.example.cookapp.database;

public enum DifficultyLevel {
    EASY("初级"),
    MEDIUM("中级"),
    HARD("高级");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 