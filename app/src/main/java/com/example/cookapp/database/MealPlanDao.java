package com.example.cookapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface MealPlanDao {
    @Insert
    long insert(MealPlan mealPlan);

    @Delete
    void delete(MealPlan mealPlan);

    @Query("DELETE FROM meal_plans WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND planDate >= :startOfDay AND planDate < :endOfDay ORDER BY mealType ASC")
    List<MealPlan> getMealPlansByUserAndDate(long userId, Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND planDate BETWEEN :startDate AND :endDate ORDER BY planDate ASC, mealType ASC")
    List<MealPlan> getMealPlansByUserAndDateRange(long userId, Date startDate, Date endDate);

    @Query("SELECT * FROM meal_plans WHERE userId = :userId ORDER BY planDate ASC, mealType ASC")
    List<MealPlan> getAllMealPlansByUser(long userId);

    @Query("DELETE FROM meal_plans WHERE userId = :userId AND planDate >= :startOfDay AND planDate < :endOfDay")
    void deleteMealPlansByUserAndDate(long userId, Date startOfDay, Date endOfDay);

    @Query("DELETE FROM meal_plans WHERE userId = :userId AND planDate BETWEEN :startDate AND :endDate")
    void deleteMealPlansByUserAndDateRange(long userId, Date startDate, Date endDate);
}
