package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
    tableName = "cooking_history",
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
        @Index(value = {"userId", "recipeId"}, unique = true)
    }
)
public class CookingHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long userId;
    private long recipeId;
    private int cookCount;
    private Date lastCookedAt;

    public CookingHistory() {
        this.cookCount = 0;
    }

    @Ignore
    public CookingHistory(long userId, long recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.cookCount = 1;
        this.lastCookedAt = new Date();
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

    public int getCookCount() {
        return cookCount;
    }

    public void setCookCount(int cookCount) {
        this.cookCount = cookCount;
    }

    public Date getLastCookedAt() {
        return lastCookedAt;
    }

    public void setLastCookedAt(Date lastCookedAt) {
        this.lastCookedAt = lastCookedAt;
    }
}
