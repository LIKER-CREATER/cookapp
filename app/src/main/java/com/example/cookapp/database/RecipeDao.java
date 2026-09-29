package com.example.cookapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecipeDao {
    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();
    
    @Query("SELECT * FROM recipes WHERE id = :id")
    Recipe getRecipeById(long id);
    
    @Query("SELECT * FROM recipes WHERE name = :name")
    Recipe getRecipeByName(String name);
    
    @Query("SELECT * FROM recipes WHERE name LIKE :searchQuery")
    List<Recipe> searchRecipes(String searchQuery);
    
    @Query("SELECT * FROM recipes WHERE cuisineType = :cuisineType")
    List<Recipe> getRecipesByCuisineType(Recipe.CuisineType cuisineType);
    
    @Query("SELECT * FROM recipes WHERE cookingMethod = :cookingMethod")
    List<Recipe> getRecipesByCookingMethod(Recipe.CookingMethod cookingMethod);
    
    @Query("SELECT * FROM recipes WHERE difficulty = :difficultyLevel")
    List<Recipe> getRecipesByDifficultyLevel(Recipe.DifficultyLevel difficultyLevel);
    
    @Insert
    void insert(Recipe recipe);
    
    @Insert
    void insertAll(List<Recipe> recipes);
    
    @Update
    void update(Recipe recipe);
    
    @Delete
    void delete(Recipe recipe);
    
    @Query("DELETE FROM recipes")
    void deleteAllRecipes();
    
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :searchQuery || '%'")
    List<Recipe> searchRecipesByName(String searchQuery);
    
    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :searchQuery || '%'")
    List<Recipe> searchRecipesByIngredients(String searchQuery);
    
    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    List<Recipe> getFavoriteRecipes();
    
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    void updateFavoriteStatus(long recipeId, boolean isFavorite);
    
    @Query("UPDATE recipes SET isShared = :isShared WHERE id = :recipeId")
    void updateSharedStatus(long recipeId, boolean isShared);
    
    @Query("SELECT * FROM recipes WHERE id IN (:recipeIds)")
    List<Recipe> getRecipesByIds(List<Long> recipeIds);
} 