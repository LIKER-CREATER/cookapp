package com.example.cookapp.ui.shopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookapp.R;
import com.example.cookapp.database.ShoppingItem;
import com.google.android.material.button.MaterialButton;

public class ShoppingListAdapter extends ListAdapter<ShoppingItem, ShoppingListAdapter.ShoppingItemViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ShoppingItem item);
        void onCheckChanged(ShoppingItem item, boolean isChecked);
        void onDeleteClick(ShoppingItem item);
    }

    public ShoppingListAdapter() {
        super(new DiffUtil.ItemCallback<ShoppingItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ShoppingItem oldItem, @NonNull ShoppingItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ShoppingItem oldItem, @NonNull ShoppingItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getQuantity() == newItem.getQuantity() &&
                       java.util.Objects.equals(oldItem.getUnit(), newItem.getUnit()) &&
                       oldItem.isChecked() == newItem.isChecked();
            }
        });
        this.listener = null;
    }

    public ShoppingListAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<ShoppingItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ShoppingItem oldItem, @NonNull ShoppingItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ShoppingItem oldItem, @NonNull ShoppingItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getQuantity() == newItem.getQuantity() &&
                       java.util.Objects.equals(oldItem.getUnit(), newItem.getUnit()) &&
                       oldItem.isChecked() == newItem.isChecked();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ShoppingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        ShoppingItem item = getItem(position);
        holder.bind(item, listener);
    }

    static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView nameText;
        private final TextView quantityText;
        private final MaterialButton deleteButton;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            nameText = itemView.findViewById(R.id.nameText);
            quantityText = itemView.findViewById(R.id.quantityText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(ShoppingItem item, OnItemClickListener listener) {
            nameText.setText(item.getName());
            String qtyText = item.getUnit() != null && !item.getUnit().isEmpty()
                    ? item.getQuantity() + " " + item.getUnit()
                    : String.valueOf(item.getQuantity());
            quantityText.setText(qtyText);
            checkBox.setChecked(item.isChecked());

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(item));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> 
                    listener.onCheckChanged(item, isChecked));
                deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
            }
        }
    }
} 