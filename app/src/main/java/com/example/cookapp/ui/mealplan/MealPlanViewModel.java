package com.example.cookapp.ui.mealplan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.CookingHistory;
import com.example.cookapp.database.CookingHistoryDao;
import com.example.cookapp.database.MealPlan;
import com.example.cookapp.database.MealPlanDao;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;
import com.example.cookapp.database.ShoppingItem;
import com.example.cookapp.database.ShoppingItemDao;
import com.example.cookapp.database.User;
import com.example.cookapp.util.IngredientParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealPlanViewModel extends AndroidViewModel {
    private final MealPlanDao mealPlanDao;
    private final RecipeDao recipeDao;
    private final ShoppingItemDao shoppingItemDao;
    private final CookingHistoryDao cookingHistoryDao;
    private final ExecutorService executor;

    private final MutableLiveData<List<Recipe>> selectedDateRecipes = new MutableLiveData<>();
    private Date selectedDate = new Date();

    public MealPlanViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        mealPlanDao = db.mealPlanDao();
        recipeDao = db.recipeDao();
        shoppingItemDao = db.shoppingItemDao();
        cookingHistoryDao = db.cookingHistoryDao();
        executor = Executors.newSingleThreadExecutor();
    }

    private Date[] getDayRange(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date endOfDay = cal.getTime();
        return new Date[]{startOfDay, endOfDay};
    }

    public void loadMealPlansForDate(long userId, Date date) {
        this.selectedDate = date;
        executor.execute(() -> {
            Date[] range = getDayRange(date);
            List<MealPlan> plans = mealPlanDao.getMealPlansByUserAndDate(userId, range[0], range[1]);
            List<Recipe> recipes = new ArrayList<>();
            for (MealPlan plan : plans) {
                Recipe recipe = recipeDao.getRecipeById(plan.getRecipeId());
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }
            selectedDateRecipes.postValue(recipes);
        });
    }

    public LiveData<List<Recipe>> getSelectedDateRecipes() {
        return selectedDateRecipes;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
    }

    public void addMealPlan(long userId, long recipeId, Date date, MealPlan.MealType mealType) {
        executor.execute(() -> {
            MealPlan plan = new MealPlan(userId, recipeId, date, mealType);
            mealPlanDao.insert(plan);
            loadMealPlansForDate(userId, date);
        });
    }

    public void deleteMealPlan(long userId, Date date, long recipeId) {
        executor.execute(() -> {
            Date[] range = getDayRange(date);
            List<MealPlan> plans = mealPlanDao.getMealPlansByUserAndDate(userId, range[0], range[1]);
            for (MealPlan plan : plans) {
                if (plan.getRecipeId() == recipeId) {
                    mealPlanDao.deleteById(plan.getId());
                    break;
                }
            }
            loadMealPlansForDate(userId, date);
        });
    }

    public void addSelectedDateToShoppingList(long userId, Date date, AddToShoppingListCallback callback) {
        executor.execute(() -> {
            Date[] range = getDayRange(date);
            List<MealPlan> plans = mealPlanDao.getMealPlansByUserAndDate(userId, range[0], range[1]);
            if (plans.isEmpty()) {
                callback.onResult(false, "当天没有计划");
                return;
            }
            addPlansToShoppingList(plans, callback);
        });
    }

    public void addWeekToShoppingList(long userId, AddToShoppingListCallback callback) {
        executor.execute(() -> {
            Date[] todayRange = getDayRange(new Date());
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayRange[1]);
            cal.add(Calendar.DAY_OF_YEAR, 7);
            Date weekLater = cal.getTime();

            List<MealPlan> plans = mealPlanDao.getMealPlansByUserAndDateRange(userId, todayRange[0], weekLater);
            if (plans.isEmpty()) {
                callback.onResult(false, "本周暂无计划");
                return;
            }
            addPlansToShoppingList(plans, callback);
        });
    }

    private void addPlansToShoppingList(List<MealPlan> plans, AddToShoppingListCallback callback) {
        Set<String> addedItems = new HashSet<>();
        long userId = -1;
        for (MealPlan plan : plans) {
            userId = plan.getUserId();
            Recipe recipe = recipeDao.getRecipeById(plan.getRecipeId());
            if (recipe == null) continue;

            // 记录烹饪历史
            CookingHistory existing = cookingHistoryDao.getByUserAndRecipe(userId, plan.getRecipeId());
            if (existing != null) {
                cookingHistoryDao.incrementCookCount(userId, plan.getRecipeId(), new Date());
            } else {
                CookingHistory history = new CookingHistory(userId, plan.getRecipeId());
                cookingHistoryDao.insert(history);
            }

            // 解析食材加入购物清单
            String[] parts = recipe.getIngredients().split("[，、\\s]+");
            for (String part : parts) {
                if (part.trim().isEmpty()) continue;
                IngredientParser.ParsedIngredient parsed = IngredientParser.parse(part.trim());
                String key = parsed.name + "_" + parsed.quantity + "_" + parsed.unit;
                if (!addedItems.contains(key)) {
                    addedItems.add(key);
                    ShoppingItem item = new ShoppingItem(plan.getRecipeId(), parsed.name, parsed.quantity, parsed.unit);
                    shoppingItemDao.insert(item);
                }
            }
        }
        callback.onResult(true, "已添加 " + addedItems.size() + " 项食材到购物清单");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public interface AddToShoppingListCallback {
        void onResult(boolean success, String message);
    }
}
