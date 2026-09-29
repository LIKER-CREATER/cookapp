package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
    tableName = "meal_plans",
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Recipe.class,
            parentColumns = "id",
            childColumns = "recipeId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("userId"),
        @Index("recipeId"),
        @Index("planDate")
    }
)
public class MealPlan {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId;
    private long recipeId;
    private Date planDate;
    private MealType mealType;

    public enum MealType {
        BREAKFAST("早餐"),
        LUNCH("午餐"),
        DINNER("晚餐");

        private final String displayName;

        MealType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public MealPlan() {
    }

    @Ignore
    public MealPlan(long userId, long recipeId, Date planDate, MealType mealType) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.planDate = planDate;
        this.mealType = mealType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public Date getPlanDate() {
        return planDate;
    }

    public void setPlanDate(Date planDate) {
        this.planDate = planDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
}
