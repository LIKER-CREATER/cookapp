package com.example.cookapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface CookingHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CookingHistory history);

    @Update
    void update(CookingHistory history);

    @Query("SELECT * FROM cooking_history WHERE userId = :userId AND recipeId = :recipeId LIMIT 1")
    CookingHistory getByUserAndRecipe(long userId, long recipeId);

    @Query("SELECT * FROM cooking_history WHERE userId = :userId ORDER BY lastCookedAt DESC")
    List<CookingHistory> getAllByUser(long userId);

    @Query("SELECT * FROM cooking_history WHERE userId = :userId ORDER BY lastCookedAt DESC LIMIT :limit")
    List<CookingHistory> getRecentByUser(long userId, int limit);

    @Query("SELECT * FROM cooking_history WHERE userId = :userId ORDER BY cookCount DESC LIMIT :limit")
    List<CookingHistory> getMostCookedByUser(long userId, int limit);

    @Query("UPDATE cooking_history SET cookCount = cookCount + 1, lastCookedAt = :date WHERE userId = :userId AND recipeId = :recipeId")
    void incrementCookCount(long userId, long recipeId, Date date);

    @Query("SELECT COUNT(*) FROM cooking_history WHERE userId = :userId AND recipeId = :recipeId")
    int countByUserAndRecipe(long userId, long recipeId);
}
