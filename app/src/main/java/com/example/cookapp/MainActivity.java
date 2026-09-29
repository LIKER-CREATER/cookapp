package com.example.cookapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.cookapp.database.Recipe;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navView;
    private int userId;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("userId", 1);
        Log.d(TAG, "MainActivity启动，userId: " + userId);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navView);

        if (drawerLayout == null || toolbar == null || navView == null) {
            Log.e(TAG, "布局文件缺失关键控件");
            Toast.makeText(this, "布局加载异常，请重装应用", Toast.LENGTH_LONG).show();
            return;
        }

        setSupportActionBar(toolbar);

        // 设置 Toolbar 左侧汉堡菜单按钮
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // 设置导航控制器
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            Log.e(TAG, "NavHostFragment 未找到");
            Toast.makeText(this, "导航初始化异常", Toast.LENGTH_SHORT).show();
            return;
        }

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        // 传递 userId 到所有 fragment
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        navController.setGraph(R.navigation.nav_graph, args);

        // 同步 Toolbar 标题与 Navigation 页面标题
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
            // 关闭侧边栏（如果打开）
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public int getUserId() {
        return userId;
    }

    public void showRecipeDetail(Recipe recipe) {
        Log.d(TAG, "显示食谱详情: " + recipe.getName() + ", ID: " + recipe.getId());
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
