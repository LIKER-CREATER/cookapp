package com.example.cookapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;
import com.example.cookapp.database.ShoppingItem;
import com.example.cookapp.database.ShoppingItemDao;
import com.example.cookapp.database.User;
import com.example.cookapp.database.Favorite;
import com.example.cookapp.database.FavoriteDao;
import com.example.cookapp.database.MealPlan;
import com.example.cookapp.util.IngredientParser;
import com.example.cookapp.util.RecipeImageLoader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModel;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";
    private ImageView recipeImage;
    private TextView recipeName;
    private TextView cuisineType;
    private TextView cookingMethod;
    private TextView difficulty;
    private TextView cookingTime;
    private TextView calories;
    private TextView ingredients;
    private TextView steps;
    private FloatingActionButton favoriteButton;
    private FloatingActionButton shareButton;
    private FloatingActionButton shoppingListButton;
    private FloatingActionButton planButton;
    private RecipeDao recipeDao;
    private ShoppingItemDao shoppingItemDao;
    private Recipe recipe;
    private ExecutorService executorService;
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        executorService = Executors.newSingleThreadExecutor();
        
        // 初始化视图
        initializeViews();
        
        // 设置工具栏
        setupToolbar();

        // 获取传递过来的食谱ID
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            finish();
            return;
        }

        // 从数据库加载食谱详情
        loadRecipeDetails(recipeId);
    }

    private void initializeViews() {
        recipeImage = findViewById(R.id.recipeImage);
        recipeName = findViewById(R.id.recipeName);
        cuisineType = findViewById(R.id.cuisineType);
        cookingMethod = findViewById(R.id.cookingMethod);
        difficulty = findViewById(R.id.difficulty);
        cookingTime = findViewById(R.id.cookingTime);
        calories = findViewById(R.id.calories);
        ingredients = findViewById(R.id.ingredients);
        steps = findViewById(R.id.steps);
        favoriteButton = findViewById(R.id.favoriteButton);
        shareButton = findViewById(R.id.shareButton);
        shoppingListButton = findViewById(R.id.shoppingListButton);
        planButton = findViewById(R.id.planButton);

        favoriteButton.setOnClickListener(v -> toggleFavorite());
        shareButton.setOnClickListener(v -> shareRecipe());
        shoppingListButton.setOnClickListener(v -> addToShoppingList());
        planButton.setOnClickListener(v -> showAddToMealPlanDialog());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_delete) {
            showDeleteConfirmDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmDialog() {
        if (recipe == null) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("删除食谱")
                .setMessage("确定要删除《" + recipe.getName() + "》吗？此操作不可恢复。")
                .setPositiveButton("删除", (dialog, which) -> {
                    executorService.execute(() -> {
                        recipeDao.delete(recipe);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "食谱已删除", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadRecipeDetails(long recipeId) {
        Log.d(TAG, "开始加载食谱详情，ID: " + recipeId);
        recipeDao = AppDatabase.getInstance(this).recipeDao();
        shoppingItemDao = AppDatabase.getInstance(this).shoppingItemDao();
        new Thread(() -> {
            recipe = recipeDao.getRecipeById(recipeId);
            if (recipe == null) {
                Log.e(TAG, "未找到食谱，ID: " + recipeId);
                runOnUiThread(() -> finish());
                return;
            }
            Log.d(TAG, "成功加载食谱: " + recipe.getName() + 
                ", 图片名称: " + recipe.getImageName());
            runOnUiThread(this::updateUI);
        }).start();
    }

    private void updateUI() {
        if (recipe == null) {
            Log.e(TAG, "updateUI: recipe 为空");
            return;
        }

        Log.d(TAG, "开始更新UI，食谱名称: " + recipe.getName());

        // 设置食谱图片
        String imageName = recipe.getImageName();
        Log.d(TAG, "加载图片: " + imageName);
        RecipeImageLoader.load(this, recipeImage, imageName);

        // 设置基本信息
        recipeName.setText(recipe.getName());
        cuisineType.setText(recipe.getCuisineType().getDisplayName());
        cookingMethod.setText(recipe.getCookingMethod().getDisplayName());
        difficulty.setText(recipe.getDifficulty().getDisplayName());
        cookingTime.setText(recipe.getCookingTime() + "分钟");
        calories.setText(recipe.getCalories() + "千卡");

        // 设置食材和步骤
        ingredients.setText(recipe.getIngredients());
        steps.setText(recipe.getSteps());

        // 检查收藏状态
        executorService.execute(() -> {
            User currentUser = AppDatabase.getInstance(this).userDao().getCurrentUser();
            if (currentUser != null) {
                boolean isFavorite = AppDatabase.getInstance(this).favoriteDao()
                        .isFavorite(currentUser.getId(), recipe.getId());
                runOnUiThread(() -> {
                    favoriteButton.setImageResource(isFavorite ? 
                        R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                });
            }
        });
    }

    private void addToShoppingList() {
        if (recipe == null) return;

        executorService.execute(() -> {
            String[] ingredientsArray = recipe.getIngredients().split("[，、\\s]+");
            List<ShoppingItem> shoppingItems = new ArrayList<>();

            for (String ingredient : ingredientsArray) {
                if (!ingredient.trim().isEmpty()) {
                    IngredientParser.ParsedIngredient parsed = IngredientParser.parse(ingredient.trim());
                    ShoppingItem item = new ShoppingItem(
                            recipe.getId(), parsed.name, parsed.quantity, parsed.unit);
                    shoppingItems.add(item);
                }
            }

            shoppingItemDao.insertAll(shoppingItems);

            runOnUiThread(() -> {
                Toast.makeText(this, "已将食材添加到购物清单", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void toggleFavorite() {
        executorService.execute(() -> {
            User currentUser = AppDatabase.getInstance(this).userDao().getCurrentUser();
            if (currentUser == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            FavoriteDao favoriteDao = AppDatabase.getInstance(this).favoriteDao();
            boolean isFavorite = favoriteDao.isFavorite(currentUser.getId(), recipe.getId());

            if (isFavorite) {
                // 取消收藏
                favoriteDao.deleteFavorite(currentUser.getId(), recipe.getId());
                runOnUiThread(() -> {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                    Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
                });
            } else {
                // 添加收藏
                Favorite favorite = new Favorite(currentUser.getId(), recipe.getId());
                favoriteDao.insert(favorite);
                runOnUiThread(() -> {
                    favoriteButton.setImageResource(R.drawable.ic_favorite);
                    Toast.makeText(this, "已添加到收藏", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void shareRecipe() {
        if (recipe == null) return;

        // 构建分享内容
        StringBuilder shareContent = new StringBuilder();
        shareContent.append("【").append(recipe.getName()).append("】\n\n");
        shareContent.append("菜系：").append(recipe.getCuisineType().getDisplayName()).append("\n");
        shareContent.append("烹饪方式：").append(recipe.getCookingMethod().getDisplayName()).append("\n");
        shareContent.append("难度：").append(recipe.getDifficulty().getDisplayName()).append("\n");
        shareContent.append("烹饪时间：").append(recipe.getCookingTime()).append("分钟\n");
        shareContent.append("热量：").append(recipe.getCalories()).append("千卡\n\n");

        shareContent.append("【食材】\n").append(recipe.getIngredients()).append("\n\n");
        shareContent.append("【步骤】\n").append(recipe.getSteps());

        // 创建分享意图
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent.toString());

        // 启动分享
        startActivity(Intent.createChooser(shareIntent, "分享食谱"));
    }

    private void showAddToMealPlanDialog() {
        executorService.execute(() -> {
            User currentUser = AppDatabase.getInstance(this).userDao().getCurrentUser();
            if (currentUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show());
                return;
            }

            String[] mealTypes = {"早餐", "午餐", "晚餐"};
            String[] dateOptions = {"今天", "明天", "后天"};

            runOnUiThread(() -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("加入菜单计划")
                        .setItems(mealTypes, (dialog, which) -> {
                            MealPlan.MealType selectedMealType = MealPlan.MealType.values()[which];

                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                                    .setTitle("选择日期")
                                    .setItems(dateOptions, (dialog2, dayOffset) -> {
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.DAY_OF_YEAR, dayOffset);
                                        cal.set(Calendar.HOUR_OF_DAY, 0);
                                        cal.set(Calendar.MINUTE, 0);
                                        cal.set(Calendar.SECOND, 0);
                                        cal.set(Calendar.MILLISECOND, 0);
                                        Date planDate = cal.getTime();

                                        executorService.execute(() -> {
                                            AppDatabase db = AppDatabase.getInstance(this);
                                            MealPlan plan = new MealPlan(
                                                    currentUser.getId(),
                                                    recipe.getId(),
                                                    planDate,
                                                    selectedMealType
                                            );
                                            db.mealPlanDao().insert(plan);
                                            runOnUiThread(() -> {
                                                String dateStr = dayOffset == 0 ? "今天" : (dayOffset == 1 ? "明天" : "后天");
                                                Toast.makeText(this,
                                                        "已加入" + dateStr + selectedMealType.getDisplayName() + "计划",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                        });
                                    })
                                    .setNegativeButton("取消", null)
                                    .show();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 