package com.example.cookapp.ui.shopping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookapp.R;
import com.example.cookapp.database.ShoppingItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ShoppingListFragment extends Fragment implements ShoppingListAdapter.OnItemClickListener {

    private ShoppingListViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MaterialButton addItemButton;
    private MaterialButton clearButton;
    private ShoppingListAdapter adapter;
    private MaterialToolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShoppingListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        addItemButton = view.findViewById(R.id.addItemButton);
        clearButton = view.findViewById(R.id.clearButton);

        setupToolbar();
        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ShoppingListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        clearButton.setOnClickListener(v -> viewModel.clearCheckedItems());
    }

    private void observeViewModel() {
        viewModel.getShoppingItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            updateEmptyView(items.isEmpty());
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showAddItemDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_shopping_item, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        TextInputEditText unitInput = dialogView.findViewById(R.id.unitInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("添加食材")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String quantityStr = quantityInput.getText().toString().trim();
                    String unit = unitInput.getText().toString().trim();
                    int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);

                    if (!name.isEmpty()) {
                        ShoppingItem item = new ShoppingItem(null, name, quantity, unit);
                        viewModel.insert(item);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onItemClick(ShoppingItem item) {
        // 可以在这里实现点击项目时的操作，比如显示编辑对话框
    }

    @Override
    public void onCheckChanged(ShoppingItem item, boolean isChecked) {
        item.setChecked(isChecked);
        viewModel.update(item);
    }

    @Override
    public void onDeleteClick(ShoppingItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除食材")
                .setMessage("确定要删除这个食材吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.delete(item))
                .setNegativeButton("取消", null)
                .show();
    }
} 