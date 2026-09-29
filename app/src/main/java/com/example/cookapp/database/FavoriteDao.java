package com.example.cookapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);
    
    @Delete
    void delete(Favorite favorite);
    
    @Query("DELETE FROM favorites WHERE userId = :userId AND recipeId = :recipeId")
    void deleteFavorite(long userId, long recipeId);
    
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    List<Favorite> getFavoritesByUserId(long userId);
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND recipeId = :recipeId)")
    boolean isFavorite(long userId, long recipeId);
} 