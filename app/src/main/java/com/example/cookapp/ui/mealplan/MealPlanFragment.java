package com.example.cookapp.ui.mealplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookapp.R;
import com.example.cookapp.RecipeDetailActivity;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.MealPlan;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealPlanFragment extends Fragment implements MealPlanAdapter.OnItemClickListener {

    private MealPlanViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate;
    private TextView emptyView;
    private MaterialButton btnAddToShoppingList;
    private MealPlanAdapter adapter;
    private User currentUser;
    private Date selectedDate = new Date();
    private List<MealPlan> currentPlans;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MealPlanViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        emptyView = view.findViewById(R.id.emptyView);
        btnAddToShoppingList = view.findViewById(R.id.btnAddToShoppingList);

        ImageButton btnPrevDay = view.findViewById(R.id.btnPrevDay);
        ImageButton btnNextDay = view.findViewById(R.id.btnNextDay);

        adapter = new MealPlanAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnPrevDay.setOnClickListener(v -> navigateDay(-1));
        btnNextDay.setOnClickListener(v -> navigateDay(1));

        btnAddToShoppingList.setOnClickListener(v -> showAddToShoppingListDialog());

        observeViewModel();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        new Thread(() -> {
            currentUser = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser();
            if (currentUser != null) {
                requireActivity().runOnUiThread(() -> viewModel.loadMealPlansForDate(currentUser.getId(), selectedDate));
            }
        }).start();
    }

    private void observeViewModel() {
        viewModel.getSelectedDateRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.submitList(recipes);
            emptyView.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(recipes.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void navigateDay(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.add(Calendar.DAY_OF_YEAR, offset);
        selectedDate = cal.getTime();
        viewModel.setSelectedDate(selectedDate);
        updateDateLabel();
        if (currentUser != null) {
            viewModel.loadMealPlansForDate(currentUser.getId(), selectedDate);
        }
    }

    private void updateDateLabel() {
        Calendar today = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        String label;
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            label = "今天";
        } else {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                label = "昨天";
            } else {
                label = dateFormat.format(selectedDate);
            }
        }
        tvSelectedDate.setText(label);
    }

    private void showAddToShoppingListDialog() {
        String[] options = {"当天计划加入购物清单", "本周计划加入购物清单"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("批量加入购物清单")
                .setItems(options, (dialog, which) -> {
                    if (currentUser == null) {
                        Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (which == 0) {
                        viewModel.addSelectedDateToShoppingList(currentUser.getId(), selectedDate,
                                (success, message) -> requireActivity().runOnUiThread(
                                        () -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()));
                    } else {
                        viewModel.addWeekToShoppingList(currentUser.getId(),
                                (success, message) -> requireActivity().runOnUiThread(
                                        () -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()));
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onItemClick(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Recipe recipe) {
        if (currentUser == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除计划")
                .setMessage("确定要从计划中移除「" + recipe.getName() + "」吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.deleteMealPlan(
                        currentUser.getId(), selectedDate, recipe.getId()))
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            viewModel.loadMealPlansForDate(currentUser.getId(), selectedDate);
        }
    }
}
