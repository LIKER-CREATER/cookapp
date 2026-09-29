package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "recipes", indices = {@Index(value = {"name"}, unique = true)})
@TypeConverters(Converters.class)
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String ingredients;
    private String steps;
    private String imageName; // 存储图片名称，对应drawable中的资源
    private boolean isFavorite;
    private boolean isShared;
    
    // 新增字段
    private CuisineType cuisineType;
    private CookingMethod cookingMethod;
    private DifficultyLevel difficulty;
    private int cookingTime;    // 烹饪时间（分钟）
    private int calories;       // 卡路里
    
    public enum CuisineType {
        SICHUAN("川菜"),
        CANTONESE("粤菜"),
        SHANDONG("鲁菜"),
        JIANGSU("苏菜"),
        ZHEJIANG("浙菜"),
        FUJIAN("闽菜"),
        HUNAN("湘菜"),
        ANHUI("徽菜"),
        NORTHEAST("东北菜"),
        NORTHWEST("西北菜"),
        SOUTHWEST("西南菜"),
        OTHER("其他");

        private final String displayName;

        CuisineType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CookingMethod {
        STIR_FRY("炒"),
        BOIL("煮"),
        STEAM("蒸"),
        DEEP_FRY("炸"),
        STEW("炖"),
        ROAST("烤"),
        BRAISE("焖"),
        PAN_FRY("煎"),
        GRILL("烤"),
        POACH("水煮"),
        OTHER("其他");

        private final String displayName;

        CookingMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DifficultyLevel {
        EASY("简单"),
        MEDIUM("中等"),
        HARD("困难");

        private final String displayName;

        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    public Recipe(String name, String ingredients, String steps, String imageName) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
        this.imageName = imageName;
        this.isFavorite = false;
        this.isShared = false;
        this.cuisineType = CuisineType.SICHUAN;  // 默认值改为川菜
        this.cookingMethod = CookingMethod.STIR_FRY;  // 默认值
        this.difficulty = DifficultyLevel.EASY;  // 默认值
        this.cookingTime = 30;  // 默认值
        this.calories = 0;  // 默认值
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    public String getSteps() {
        return steps;
    }
    
    public void setSteps(String steps) {
        this.steps = steps;
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    public boolean isShared() {
        return isShared;
    }
    
    public void setShared(boolean shared) {
        isShared = shared;
    }

    // 新增字段的 Getters and Setters
    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(CuisineType cuisineType) {
        this.cuisineType = cuisineType;
    }

    public CookingMethod getCookingMethod() {
        return cookingMethod;
    }

    public void setCookingMethod(CookingMethod cookingMethod) {
        this.cookingMethod = cookingMethod;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
} 