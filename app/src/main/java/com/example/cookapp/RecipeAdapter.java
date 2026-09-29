package com.example.cookapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;
import com.example.cookapp.database.User;
import com.example.cookapp.database.Favorite;
import com.example.cookapp.database.FavoriteDao;
import com.example.cookapp.util.RecipeImageLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private static final String TAG = "RecipeAdapter";
    private List<Recipe> recipeList;
    private OnRecipeClickListener onRecipeClickListener;
    private RecipeDao recipeDao;
    private FavoriteDao favoriteDao;
    private Context context;
    private ExecutorService executorService;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = new ArrayList<>(recipeList);
        this.onRecipeClickListener = listener;
        this.recipeDao = AppDatabase.getInstance(context).recipeDao();
        this.favoriteDao = AppDatabase.getInstance(context).favoriteDao();
        this.executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "适配器创建，初始食谱数量: " + recipeList.size());
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        
        // 设置基本信息
        holder.recipeName.setText(recipe.getName());
        holder.cuisineTypeChip.setText(recipe.getCuisineType().getDisplayName());
        holder.difficultyChip.setText(recipe.getDifficulty().getDisplayName());
        holder.cookingTime.setText(recipe.getCookingTime() + "分钟");
        holder.calories.setText(recipe.getCalories() + "千卡");

        // 加载食谱图片
        RecipeImageLoader.load(context, holder.recipeImage, recipe.getImageName());

        // 检查收藏状态
        executorService.execute(() -> {
            User currentUser = AppDatabase.getInstance(context).userDao().getCurrentUser();
            if (currentUser != null) {
                boolean isFavorite = favoriteDao.isFavorite(currentUser.getId(), recipe.getId());
                ((android.app.Activity) context).runOnUiThread(() -> {
                    holder.favoriteButton.setImageResource(isFavorite ? 
                        R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                });
            }
        });

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onRecipeClickListener != null) {
                onRecipeClickListener.onRecipeClick(recipe);
            }
        });

        // 设置收藏按钮点击事件
        holder.favoriteButton.setOnClickListener(v -> {
            executorService.execute(() -> {
                User currentUser = AppDatabase.getInstance(context).userDao().getCurrentUser();
                if (currentUser == null) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                boolean isFavorite = favoriteDao.isFavorite(currentUser.getId(), recipe.getId());
                if (isFavorite) {
                    // 取消收藏
                    favoriteDao.deleteFavorite(currentUser.getId(), recipe.getId());
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(context, "已取消收藏", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // 添加收藏
                    Favorite favorite = new Favorite(currentUser.getId(), recipe.getId());
                    favoriteDao.insert(favorite);
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(context, "已添加到收藏", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        // 设置分享按钮点击事件
        holder.shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "【" + recipe.getName() + "】\n\n" +
                "菜系：" + recipe.getCuisineType().getDisplayName() + "\n" +
                "烹饪方式：" + recipe.getCookingMethod().getDisplayName() + "\n" +
                "难度：" + recipe.getDifficulty().getDisplayName() + "\n" +
                "烹饪时间：" + recipe.getCookingTime() + "分钟\n\n" +
                "【食材】\n" + recipe.getIngredients() + "\n\n" +
                "【步骤】\n" + recipe.getSteps());
            context.startActivity(Intent.createChooser(shareIntent, "分享食谱"));
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipeList = new ArrayList<>(newRecipes);
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;
        TextView cuisineTypeChip;
        TextView difficultyChip;
        TextView cookingTime;
        TextView calories;
        ImageButton favoriteButton;
        ImageButton shareButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            cuisineTypeChip = itemView.findViewById(R.id.cuisineTypeChip);
            difficultyChip = itemView.findViewById(R.id.difficultyChip);
            cookingTime = itemView.findViewById(R.id.cookingTime);
            calories = itemView.findViewById(R.id.calories);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            shareButton = itemView.findViewById(R.id.shareButton);
        }
    }

    public void onDestroy() {
        executorService.shutdown();
    }
} 