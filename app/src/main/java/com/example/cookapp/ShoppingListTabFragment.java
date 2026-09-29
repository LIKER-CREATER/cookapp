package com.example.cookapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.ShoppingItem;
import com.example.cookapp.database.ShoppingItemDao;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingListTabFragment extends Fragment {
    private static final String ARG_IS_PURCHASED = "is_purchased";
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ShoppingItemDao shoppingItemDao;
    private ShoppingListAdapter adapter;
    private ExecutorService executorService;
    private boolean isPurchased;

    public static ShoppingListTabFragment newInstance(boolean isPurchased) {
        ShoppingListTabFragment fragment = new ShoppingListTabFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_PURCHASED, isPurchased);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPurchased = getArguments().getBoolean(ARG_IS_PURCHASED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list_tab, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        
        executorService = Executors.newSingleThreadExecutor();
        shoppingItemDao = AppDatabase.getInstance(requireContext()).shoppingItemDao();
        
        setupRecyclerView();
        loadShoppingItems();
        
        return view;
    }

    private void setupRecyclerView() {
        adapter = new ShoppingListAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadShoppingItems() {
        LiveData<List<ShoppingItem>> itemsLiveData = isPurchased ? 
            shoppingItemDao.getCheckedItems() : shoppingItemDao.getUncheckedItems();
            
        itemsLiveData.observe(getViewLifecycleOwner(), items -> {
            adapter.updateItems(items);
            updateEmptyView(items.isEmpty());
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {
        private List<ShoppingItem> items;

        public ShoppingListAdapter(List<ShoppingItem> items) {
            this.items = items;
        }

        public void updateItems(List<ShoppingItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShoppingItem item = items.get(position);
            
            // 设置食材图标
            View ingredientIconView = holder.itemView.findViewById(R.id.ingredientIcon);
            TextView ingredientNameView = ingredientIconView.findViewById(R.id.ingredientName);
            ingredientNameView.setText(item.getName().substring(0, Math.min(2, item.getName().length())));

            holder.itemNameText.setText(item.getName());
            holder.itemQuantityText.setText(item.getQuantity());
            holder.itemCheckBox.setChecked(item.isChecked());

            holder.itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                executorService.execute(() -> {
                    item.setChecked(isChecked);
                    shoppingItemDao.update(item);
                });
            });

            holder.deleteButton.setOnClickListener(v -> {
                executorService.execute(() -> {
                    shoppingItemDao.delete(item);
                    loadShoppingItems();
                });
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox itemCheckBox;
            TextView itemNameText;
            TextView itemQuantityText;
            ImageButton deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
                itemNameText = itemView.findViewById(R.id.itemNameText);
                itemQuantityText = itemView.findViewById(R.id.itemQuantityText);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 