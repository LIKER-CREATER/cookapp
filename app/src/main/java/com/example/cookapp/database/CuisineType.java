package com.example.cookapp.database;

public enum CuisineType {
    CHINESE("中餐"),
    WESTERN("西餐"),
    JAPANESE("日料"),
    KOREAN("韩餐"),
    SOUTHEAST_ASIAN("东南亚"),
    DESSERT("甜点");

    private final String displayName;

    CuisineType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 