package com.example.cookapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.RecipeDao;
import com.example.cookapp.database.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private RecipeDao recipeDao;
    private ExecutorService executorService;
    private EditText searchEditText;
    private Spinner cuisineSpinner;
    private Spinner cookingMethodSpinner;
    private Spinner difficultySpinner;
    private static final int PICK_IMAGE = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final String PREF_NAME = "cookapp_prefs";
    private static final String KEY_SAMPLE_LOADED = "sample_recipes_loaded";
    private Uri selectedImageUri;
    private String selectedImagePath;
    private AlertDialog currentDialog;

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        recipeDao = AppDatabase.getInstance(requireContext()).recipeDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(getContext(), recipeList, this);
        recyclerView.setAdapter(adapter);

        // 初始化搜索和筛选控件
        searchEditText = view.findViewById(R.id.searchEditText);
        cuisineSpinner = view.findViewById(R.id.cuisineSpinner);
        cookingMethodSpinner = view.findViewById(R.id.cookingMethodSpinner);
        difficultySpinner = view.findViewById(R.id.difficultySpinner);

        // 设置下拉框适配器
        ArrayAdapter<CharSequence> cuisineAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getCuisineTypes());
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(cuisineAdapter);

        ArrayAdapter<CharSequence> cookingMethodAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getCookingMethods());
        cookingMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cookingMethodSpinner.setAdapter(cookingMethodAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getDifficultyLevels());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // 设置搜索监听
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 设置筛选监听
        cuisineSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterRecipes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        cookingMethodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterRecipes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        difficultySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterRecipes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        FloatingActionButton addButton = view.findViewById(R.id.addRecipeFab);
        addButton.setOnClickListener(v -> showAddRecipeDialog());

        // 加载数据
        loadRecipes();

        return view;
    }

    private void loadSampleRecipes() {
        try {
            if (recipeDao.getAllRecipes().isEmpty()) {
                Log.d("RecipesFragment", "开始加载示例数据");
                Recipe recipe1 = new Recipe(
                    "麻婆豆腐",
                    "豆腐 500g\n猪肉末 200g\n豆瓣酱 2勺\n花椒 适量\n辣椒 适量",
                    "1. 豆腐切块\n2. 热锅下油，爆香花椒\n3. 加入肉末翻炒\n4. 加入豆瓣酱炒香\n5. 加入豆腐块\n6. 调味后即可出锅",
                    "mapo_tofu"
                );
                recipe1.setCuisineType(Recipe.CuisineType.SICHUAN);
                recipe1.setCookingMethod(Recipe.CookingMethod.STIR_FRY);
                recipe1.setDifficulty(Recipe.DifficultyLevel.MEDIUM);
                recipe1.setCookingTime(30);
                recipe1.setCalories(350);

                Recipe recipe2 = new Recipe(
                    "清蒸鲈鱼",
                    "鲈鱼 1条\n姜 适量\n葱 适量\n料酒 适量\n盐 适量",
                    "1. 鱼洗净，划几刀\n2. 加入姜葱料酒腌制\n3. 上锅蒸8-10分钟\n4. 淋上热油即可",
                    "steamed_fish"
                );
                recipe2.setCuisineType(Recipe.CuisineType.CANTONESE);
                recipe2.setCookingMethod(Recipe.CookingMethod.STEAM);
                recipe2.setDifficulty(Recipe.DifficultyLevel.EASY);
                recipe2.setCookingTime(20);
                recipe2.setCalories(200);

                List<Recipe> recipes = new ArrayList<>();
                recipes.add(recipe1);
                recipes.add(recipe2);

                recipeDao.insertAll(recipes);
                Log.d("RecipesFragment", "示例数据加载完成");
            }
        } catch (Exception e) {
            Log.e("RecipesFragment", "加载示例数据失败", e);
            Toast.makeText(getContext(), "加载示例数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getCuisineTypes() {
        return new String[]{"全部", "川菜", "粤菜", "鲁菜", "苏菜", "浙菜", "闽菜", "湘菜", "徽菜"};
    }

    private String[] getCookingMethods() {
        return new String[]{"全部", "炒", "煮", "蒸", "炸", "炖", "烤", "焖", "煎"};
    }

    private String[] getDifficultyLevels() {
        return new String[]{"全部", "简单", "中等", "困难"};
    }

    private void filterRecipes() {
        String searchText = searchEditText.getText().toString().trim();
        String selectedCuisine = cuisineSpinner.getSelectedItem().toString();
        String selectedMethod = cookingMethodSpinner.getSelectedItem().toString();
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();

        executorService.execute(() -> {
            try {
                List<Recipe> filteredList = recipeDao.getAllRecipes();

                // 应用搜索过滤
                if (!searchText.isEmpty()) {
                    List<Recipe> searchResults = new ArrayList<>();
                    for (Recipe recipe : filteredList) {
                        if (recipe.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                            recipe.getIngredients().toLowerCase().contains(searchText.toLowerCase())) {
                            searchResults.add(recipe);
                        }
                    }
                    filteredList = searchResults;
                }

                // 应用菜系过滤
                if (!selectedCuisine.equals("全部")) {
                    Recipe.CuisineType cuisineType = getCuisineTypeFromString(selectedCuisine);
                    if (cuisineType != null) {
                        List<Recipe> cuisineResults = new ArrayList<>();
                        for (Recipe recipe : filteredList) {
                            if (recipe.getCuisineType() == cuisineType) {
                                cuisineResults.add(recipe);
                            }
                        }
                        filteredList = cuisineResults;
                    }
                }

                // 应用烹饪方式过滤
                if (!selectedMethod.equals("全部")) {
                    Recipe.CookingMethod cookingMethod = getCookingMethodFromString(selectedMethod);
                    if (cookingMethod != null) {
                        List<Recipe> methodResults = new ArrayList<>();
                        for (Recipe recipe : filteredList) {
                            if (recipe.getCookingMethod() == cookingMethod) {
                                methodResults.add(recipe);
                            }
                        }
                        filteredList = methodResults;
                    }
                }

                // 应用难度等级过滤
                if (!selectedDifficulty.equals("全部")) {
                    Recipe.DifficultyLevel difficultyLevel = getDifficultyLevelFromString(selectedDifficulty);
                    if (difficultyLevel != null) {
                        List<Recipe> difficultyResults = new ArrayList<>();
                        for (Recipe recipe : filteredList) {
                            if (recipe.getDifficulty() == difficultyLevel) {
                                difficultyResults.add(recipe);
                            }
                        }
                        filteredList = difficultyResults;
                    }
                }

                // 更新UI
                final List<Recipe> finalFilteredList = filteredList;
                requireActivity().runOnUiThread(() -> {
                    recipeList.clear();
                    recipeList.addAll(finalFilteredList);
                    adapter.updateRecipes(finalFilteredList);
                });
            } catch (Exception e) {
                Log.e("RecipesFragment", "筛选食谱失败", e);
            }
        });
    }

    private Recipe.CuisineType getCuisineTypeFromString(String cuisine) {
        switch (cuisine) {
            case "川菜": return Recipe.CuisineType.SICHUAN;
            case "粤菜": return Recipe.CuisineType.CANTONESE;
            case "鲁菜": return Recipe.CuisineType.SHANDONG;
            case "苏菜": return Recipe.CuisineType.JIANGSU;
            case "浙菜": return Recipe.CuisineType.ZHEJIANG;
            case "闽菜": return Recipe.CuisineType.FUJIAN;
            case "湘菜": return Recipe.CuisineType.HUNAN;
            case "徽菜": return Recipe.CuisineType.ANHUI;
            default: return null;
        }
    }

    private Recipe.CookingMethod getCookingMethodFromString(String method) {
        switch (method) {
            case "炒": return Recipe.CookingMethod.STIR_FRY;
            case "煮": return Recipe.CookingMethod.BOIL;
            case "蒸": return Recipe.CookingMethod.STEAM;
            case "炸": return Recipe.CookingMethod.DEEP_FRY;
            case "炖": return Recipe.CookingMethod.STEW;
            case "烤": return Recipe.CookingMethod.ROAST;
            case "焖": return Recipe.CookingMethod.BRAISE;
            case "煎": return Recipe.CookingMethod.PAN_FRY;
            default: return null;
        }
    }

    private Recipe.DifficultyLevel getDifficultyLevelFromString(String difficulty) {
        switch (difficulty) {
            case "简单": return Recipe.DifficultyLevel.EASY;
            case "中等": return Recipe.DifficultyLevel.MEDIUM;
            case "困难": return Recipe.DifficultyLevel.HARD;
            default: return null;
        }
    }

    private void loadRecipes() {
        executorService.execute(() -> {
            try {
                final List<Recipe> recipes = recipeDao.getAllRecipes();
                Log.d("RecipesFragment", "加载到 " + recipes.size() + " 个食谱");

                SharedPreferences prefs = requireContext()
                        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                boolean sampleLoaded = prefs.getBoolean(KEY_SAMPLE_LOADED, false);

                if (recipes.isEmpty() && !sampleLoaded) {
                    Log.d("RecipesFragment", "首次初始化，加载示例数据");
                    loadSampleRecipes();
                    prefs.edit().putBoolean(KEY_SAMPLE_LOADED, true).apply();
                    final List<Recipe> updatedRecipes = recipeDao.getAllRecipes();
                    requireActivity().runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(updatedRecipes);
                        adapter.updateRecipes(updatedRecipes);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        recipeList.clear();
                        recipeList.addAll(recipes);
                        adapter.updateRecipes(recipes);
                    });
                }
            } catch (Exception e) {
                Log.e("RecipesFragment", "加载食谱失败", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "加载食谱失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_recipe, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText ingredientsEditText = dialogView.findViewById(R.id.ingredientsEditText);
        EditText stepsEditText = dialogView.findViewById(R.id.stepsEditText);
        Spinner cuisineSpinner = dialogView.findViewById(R.id.cuisineSpinner);
        Spinner cookingMethodSpinner = dialogView.findViewById(R.id.cookingMethodSpinner);
        Spinner difficultySpinner = dialogView.findViewById(R.id.difficultySpinner);
        EditText cookingTimeEditText = dialogView.findViewById(R.id.cookingTimeEditText);
        EditText caloriesEditText = dialogView.findViewById(R.id.caloriesEditText);
        ImageView recipeImageView = dialogView.findViewById(R.id.recipeImageView);

        // 设置下拉框适配器
        ArrayAdapter<CharSequence> cuisineAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getCuisineTypes());
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(cuisineAdapter);

        ArrayAdapter<CharSequence> cookingMethodAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getCookingMethods());
        cookingMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cookingMethodSpinner.setAdapter(cookingMethodAdapter);

        ArrayAdapter<CharSequence> difficultyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, getDifficultyLevels());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // 设置图片选择
        recipeImageView.setOnClickListener(v -> {
            try {
                if (checkStoragePermission()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, PICK_IMAGE);
                } else {
                    requestStoragePermission();
                }
            } catch (Exception e) {
                Log.e("RecipesFragment", "打开图片选择器失败", e);
                Toast.makeText(getContext(), "无法打开图片选择器: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setTitle("添加新食谱");
        builder.setPositiveButton("添加", null);
        builder.setNegativeButton("取消", (dialog, which) -> {
            selectedImageUri = null;
            selectedImagePath = null;
            currentDialog = null;
        });

        currentDialog = builder.create();
        currentDialog.show();

        currentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String ingredients = ingredientsEditText.getText().toString();
            String steps = stepsEditText.getText().toString();
            String cuisine = cuisineSpinner.getSelectedItemPosition() == 0 ? null : cuisineSpinner.getSelectedItem().toString();
            String cookingMethod = cookingMethodSpinner.getSelectedItemPosition() == 0 ? null : cookingMethodSpinner.getSelectedItem().toString();
            String difficulty = difficultySpinner.getSelectedItemPosition() == 0 ? null : difficultySpinner.getSelectedItem().toString();
            String cookingTimeStr = cookingTimeEditText.getText().toString();
            String caloriesStr = caloriesEditText.getText().toString();

            if (name.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || cookingTimeStr.isEmpty()) {
                Toast.makeText(getContext(), "请填写所有必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            int cookingTime = Integer.parseInt(cookingTimeStr);
            int calories = caloriesStr.isEmpty() ? 0 : Integer.parseInt(caloriesStr);

            String imageFileName = "recipe_" + System.currentTimeMillis() + ".jpg";

            Recipe recipe = new Recipe(name, ingredients, steps, imageFileName);
            recipe.setCuisineType(getCuisineTypeFromString(cuisine));
            recipe.setCookingMethod(getCookingMethodFromString(cookingMethod));
            recipe.setDifficulty(getDifficultyLevelFromString(difficulty));
            recipe.setCookingTime(cookingTime);
            recipe.setCalories(calories);

            executorService.execute(() -> {
                try {
                    if (selectedImageUri != null) {
                        requireActivity().getContentResolver().takePersistableUriPermission(
                            selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        saveImageToInternalStorage(selectedImageUri, imageFileName);
                    }

                    recipeDao.insert(recipe);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                        loadRecipes();
                        selectedImageUri = null;
                        selectedImagePath = null;
                        currentDialog.dismiss();
                        currentDialog = null;
                    });
                } catch (Exception e) {
                    Log.e("RecipesFragment", "保存食谱失败", e);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return requireContext().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED;
        } else {
            return requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    requireActivity().getContentResolver().takePersistableUriPermission(
                        selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (currentDialog != null) {
                        ImageView recipeImageView = currentDialog.findViewById(R.id.recipeImageView);
                        if (recipeImageView != null) {
                            Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.placeholder_image)
                                .centerCrop()
                                .into(recipeImageView);
                        }
                    }

                    Toast.makeText(getContext(), "图片已选择", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("RecipesFragment", "选择图片失败", e);
                Toast.makeText(getContext(), "选择图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToInternalStorage(Uri imageUri, String fileName) throws IOException {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                throw new IOException("无法打开图片文件");
            }

            File outputDir = new File(requireActivity().getFilesDir(), "recipe_images");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            if (!outputFile.exists()) {
                throw new IOException("图片保存失败，文件不存在");
            }
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e("RecipesFragment", "关闭流失败", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAddRecipeDialog();
            } else {
                Toast.makeText(getContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
