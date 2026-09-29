package com.example.cookapp.database;

public enum CookingMethod {
    STIR_FRY("炒"),
    STEAM("蒸"),
    BOIL("煮"),
    FRY("炸"),
    ROAST("烤"),
    STEW("炖"),
    GRILL("烤"),
    COLD_DISH("凉拌");

    private final String displayName;

    CookingMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 