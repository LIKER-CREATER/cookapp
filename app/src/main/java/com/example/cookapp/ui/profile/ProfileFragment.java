package com.example.cookapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.cookapp.LoginActivity;
import com.example.cookapp.R;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private TextView usernameText;
    private LinearLayout changePasswordButton;
    private LinearLayout shoppingListButton;
    private LinearLayout logoutButton;
    private AppDatabase database;
    private ExecutorService executor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameText = view.findViewById(R.id.usernameText);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        shoppingListButton = view.findViewById(R.id.shoppingListButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        setupButtons();
        loadUserInfo();
    }

    private void setupButtons() {
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        shoppingListButton.setOnClickListener(v -> navigateToShoppingList());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserInfo() {
        executor.execute(() -> {
            User currentUser = database.userDao().getCurrentUser();
            if (currentUser != null) {
                requireActivity().runOnUiThread(() ->
                    usernameText.setText(currentUser.getUsername())
                );
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("修改密码")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String oldPassword = oldPasswordInput.getText().toString();
                String newPassword = newPasswordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(requireContext(), "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                executor.execute(() -> {
                    User currentUser = database.userDao().getCurrentUser();
                    if (currentUser != null && currentUser.getPassword().equals(oldPassword)) {
                        currentUser.setPassword(newPassword);
                        database.userDao().update(currentUser);
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "密码修改成功", Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "当前密码错误", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void navigateToShoppingList() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_profileFragment_to_shoppingListFragment);
    }

    private void logout() {
        executor.execute(() -> {
            User currentUser = database.userDao().getCurrentUser();
            if (currentUser != null) {
                database.userDao().logout(currentUser.getId());
                requireActivity().runOnUiThread(() -> {
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
