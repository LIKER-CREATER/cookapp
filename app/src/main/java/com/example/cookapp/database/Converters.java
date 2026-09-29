package com.example.cookapp.database;

import androidx.room.TypeConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static MealPlan.MealType toMealType(String value) {
        return value == null ? null : MealPlan.MealType.valueOf(value);
    }

    @TypeConverter
    public static String fromMealType(MealPlan.MealType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer i : list) {
            sb.append(i).append(",");
        }
        return sb.toString();
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }
        List<Integer> list = new ArrayList<>();
        String[] array = data.split(",");
        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(Integer.parseInt(s));
            }
        }
        return list;
    }

    @TypeConverter
    public static Recipe.CuisineType toCuisineType(String value) {
        return value == null ? null : Recipe.CuisineType.valueOf(value);
    }

    @TypeConverter
    public static String fromCuisineType(Recipe.CuisineType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Recipe.CookingMethod toCookingMethod(String value) {
        return value == null ? null : Recipe.CookingMethod.valueOf(value);
    }

    @TypeConverter
    public static String fromCookingMethod(Recipe.CookingMethod method) {
        return method == null ? null : method.name();
    }

    @TypeConverter
    public static Recipe.DifficultyLevel toDifficultyLevel(String value) {
        return value == null ? null : Recipe.DifficultyLevel.valueOf(value);
    }

    @TypeConverter
    public static String fromDifficultyLevel(Recipe.DifficultyLevel level) {
        return level == null ? null : level.name();
    }
} 