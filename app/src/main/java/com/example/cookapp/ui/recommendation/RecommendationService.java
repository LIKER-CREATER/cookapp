package com.example.cookapp.ui.recommendation;

import com.example.cookapp.database.CookingHistory;
import com.example.cookapp.database.CookingHistoryDao;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {
    private final RecipeDao recipeDao;
    private final CookingHistoryDao cookingHistoryDao;
    private final long userId;

    public RecommendationService(RecipeDao recipeDao, CookingHistoryDao cookingHistoryDao, long userId) {
        this.recipeDao = recipeDao;
        this.cookingHistoryDao = cookingHistoryDao;
        this.userId = userId;
    }

    /**
     * 加载并评分所有食谱，返回排序后的完整列表（评分 > 0）。
     * 已在内存中计算完毕，可直接本地过滤，无需再查库。
     */
    public List<RecipeWithReason> getRecommendations() {
        List<Recipe> allRecipes = recipeDao.getAllRecipes();
        if (allRecipes.isEmpty()) return Collections.emptyList();

        Map<Long, CookingHistory> historyMap = new HashMap<>();
        List<CookingHistory> histories = cookingHistoryDao.getAllByUser(userId);
        for (CookingHistory h : histories) {
            historyMap.put(h.getRecipeId(), h);
        }

        List<RecipeWithReason> scored = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            String reason = null;
            int score = 0;

            CookingHistory history = historyMap.get(recipe.getId());

            if (history != null && history.getCookCount() >= 2) {
                score += 10 + Math.min(history.getCookCount() * 2, 10);
            }

            if (history != null && history.getLastCookedAt() != null) {
                long daysSince = (System.currentTimeMillis() - history.getLastCookedAt().getTime()) / (1000 * 86400);
                if (daysSince > 7) {
                    score += Math.min((int) daysSince / 7, 5);
                    reason = "很久没做了，怀念这个味道";
                }
            }

            if (recipe.getDifficulty() == Recipe.DifficultyLevel.EASY && recipe.getCookingTime() <= 30) {
                score += 5;
                if (reason == null) reason = "简单快手，适合新手";
            }

            if (recipe.getCookingTime() <= 15) {
                score += 3;
                if (reason == null) reason = "15分钟快手菜";
            }

            if (history == null && recipe.getDifficulty() == Recipe.DifficultyLevel.EASY) {
                score += 2;
                if (reason == null) reason = "新手友好";
            }

            if (history == null) {
                score += 1;
            }

            if (score > 0) {
                scored.add(new RecipeWithReason(recipe, score, reason != null ? reason : "为你推荐"));
            }
        }

        Collections.sort(scored, (a, b) -> Integer.compare(b.score, a.score));
        return scored;
    }

    /**
     * 从已加载的推荐列表中提取精选推荐（前 N 条，固定数量）。
     */
    public static List<RecipeWithReason> pickTop(List<RecipeWithReason> all, int count) {
        if (all.isEmpty() || count <= 0) return Collections.emptyList();
        int end = Math.min(count, all.size());
        return new ArrayList<>(all.subList(0, end));
    }

    /**
     * 本地过滤（不查库，直接对已加载列表过滤）。
     * null filter 表示不过滤，返回原列表引用。
     */
    public static List<RecipeWithReason> filter(List<RecipeWithReason> all, RecommendationFilter filter) {
        if (filter == null) return all;
        List<RecipeWithReason> result = new ArrayList<>();
        for (RecipeWithReason r : all) {
            if (filter.accept(r)) result.add(r);
        }
        return result;
    }

    /**
     * 按菜系本地过滤（不查库）。
     * null cuisine 表示不过滤，返回原列表引用。
     */
    public static List<RecipeWithReason> filterByCuisine(
            List<RecipeWithReason> all, Recipe.CuisineType cuisine) {
        if (cuisine == null) return all;
        List<RecipeWithReason> result = new ArrayList<>();
        for (RecipeWithReason r : all) {
            if (r.recipe.getCuisineType() == cuisine) result.add(r);
        }
        return result;
    }

    public static class RecipeWithReason {
        public final Recipe recipe;
        public final int score;
        public final String reason;

        public RecipeWithReason(Recipe recipe, int score, String reason) {
            this.recipe = recipe;
            this.score = score;
            this.reason = reason;
        }
    }

    public interface RecommendationFilter {
        boolean accept(RecipeWithReason item);
    }
}
