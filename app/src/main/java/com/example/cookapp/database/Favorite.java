package com.example.cookapp.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites",
        primaryKeys = {"userId", "recipeId"},
        foreignKeys = {
            @ForeignKey(entity = User.class,
                    parentColumns = "id",
                    childColumns = "userId",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Recipe.class,
                    parentColumns = "id",
                    childColumns = "recipeId",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("userId"),
            @Index("recipeId")
        })
public class Favorite {
    private long userId;
    private long recipeId;
    
    public Favorite(long userId, long recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
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
} 