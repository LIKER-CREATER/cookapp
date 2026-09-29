package com.example.cookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;
import com.example.cookapp.database.User;
import com.example.cookapp.database.Favorite;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView emptyView;
    private RecipeDao recipeDao;
    private ExecutorService executor;
    private List<Recipe> allFavoriteRecipes;
    private ChipGroup cuisineTypeChipGroup;
    private ChipGroup difficultyChipGroup;

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        
        // 初始化视图
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        cuisineTypeChipGroup = view.findViewById(R.id.cuisineTypeChipGroup);
        difficultyChipGroup = view.findViewById(R.id.difficultyChipGroup);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // 初始化数据库和线程池
        recipeDao = AppDatabase.getInstance(requireContext()).recipeDao();
        executor = Executors.newSingleThreadExecutor();

        // 设置筛选监听器
        setupFilterListeners();

        // 加载收藏的食谱
        loadFavoriteRecipes();

        return view;
    }

    private void setupFilterListeners() {
        // 菜系筛选
        cuisineTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                applyFilters();
            }
        });

        // 难度筛选
        difficultyChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                applyFilters();
            }
        });
    }

    private void loadFavoriteRecipes() {
        executor.execute(() -> {
            User currentUser = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser();
            if (currentUser != null) {
                List<Favorite> favorites = AppDatabase.getInstance(requireContext()).favoriteDao().getFavoritesByUserId(currentUser.getId());
                List<Recipe> recipes = new ArrayList<>();
                for (Favorite favorite : favorites) {
                    Recipe recipe = recipeDao.getRecipeById(favorite.getRecipeId());
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                }
                allFavoriteRecipes = recipes;
                requireActivity().runOnUiThread(this::applyFilters);
            }
        });
    }

    private void applyFilters() {
        if (allFavoriteRecipes == null) return;

        List<Recipe> filteredRecipes = new ArrayList<>(allFavoriteRecipes);

        // 获取选中的筛选条件
        Recipe.CuisineType selectedCuisineType = getSelectedCuisineType();
        Recipe.DifficultyLevel selectedDifficulty = getSelectedDifficulty();

        // 应用筛选
        filteredRecipes.removeIf(recipe -> {
            boolean cuisineTypeMatch = selectedCuisineType == null || recipe.getCuisineType() == selectedCuisineType;
            boolean difficultyMatch = selectedDifficulty == null || recipe.getDifficulty() == selectedDifficulty;
            return !(cuisineTypeMatch && difficultyMatch);
        });

        // 更新UI
        updateUI(filteredRecipes);
    }

    private Recipe.CuisineType getSelectedCuisineType() {
        int selectedChipId = cuisineTypeChipGroup.getCheckedChipId();
        if (selectedChipId == View.NO_ID || selectedChipId == R.id.chipAllCuisine) {
            return null;
        }

        Chip selectedChip = cuisineTypeChipGroup.findViewById(selectedChipId);
        String cuisineType = selectedChip.getText().toString();
        
        for (Recipe.CuisineType type : Recipe.CuisineType.values()) {
            if (type.getDisplayName().equals(cuisineType)) {
                return type;
            }
        }
        return null;
    }

    private Recipe.DifficultyLevel getSelectedDifficulty() {
        int selectedChipId = difficultyChipGroup.getCheckedChipId();
        if (selectedChipId == View.NO_ID || selectedChipId == R.id.chipAllDifficulty) {
            return null;
        }

        Chip selectedChip = difficultyChipGroup.findViewById(selectedChipId);
        String difficulty = selectedChip.getText().toString();
        
        for (Recipe.DifficultyLevel level : Recipe.DifficultyLevel.values()) {
            if (level.getDisplayName().equals(difficulty)) {
                return level;
            }
        }
        return null;
    }

    private void updateUI(List<Recipe> recipes) {
        adapter.updateRecipes(recipes);
        emptyView.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(recipes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
} 