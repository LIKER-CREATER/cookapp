package com.example.cookapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ShoppingItemDao {
    @Insert
    void insert(ShoppingItem item);

    @Insert
    void insertAll(List<ShoppingItem> items);

    @Update
    void update(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    void deleteCheckedItems();

    @Query("SELECT * FROM shopping_items ORDER BY id DESC")
    LiveData<List<ShoppingItem>> getAllItems();

    @Query("SELECT * FROM shopping_items WHERE recipeId = :recipeId")
    List<ShoppingItem> getItemsByRecipeId(long recipeId);

    @Query("SELECT * FROM shopping_items WHERE isChecked = 0 ORDER BY id DESC")
    LiveData<List<ShoppingItem>> getUncheckedItems();

    @Query("SELECT * FROM shopping_items WHERE isChecked = 1 ORDER BY id DESC")
    LiveData<List<ShoppingItem>> getCheckedItems();

    @Query("DELETE FROM shopping_items WHERE recipeId = :recipeId")
    void deleteItemsByRecipeId(long recipeId);

    @Query("UPDATE shopping_items SET isChecked = :isChecked WHERE id = :itemId")
    void updateCheckedStatus(long itemId, boolean isChecked);

    @Query("DELETE FROM shopping_items WHERE isChecked = :isChecked")
    void deleteByStatus(boolean isChecked);
} 