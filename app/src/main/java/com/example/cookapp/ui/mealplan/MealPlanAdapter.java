package com.example.cookapp.ui.mealplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookapp.R;
import com.example.cookapp.database.Recipe;

public class MealPlanAdapter extends ListAdapter<Recipe, MealPlanAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
        void onDeleteClick(Recipe recipe);
    }

    private final OnItemClickListener listener;

    public MealPlanAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Recipe>() {
            @Override
            public boolean areItemsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = getItem(position);
        holder.bind(recipe);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImage;
        private final TextView tvMealType;
        private final TextView tvRecipeName;
        private final TextView tvRecipeInfo;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeInfo = itemView.findViewById(R.id.tvRecipeInfo);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Recipe recipe) {
            tvRecipeName.setText(recipe.getName());
            tvMealType.setText("计划");
            String info = recipe.getCuisineType().getDisplayName()
                    + " · " + recipe.getCookingMethod().getDisplayName()
                    + " · " + recipe.getDifficulty().getDisplayName()
                    + " · " + recipe.getCookingTime() + "分钟";
            tvRecipeInfo.setText(info);

            String imageName = recipe.getImageName();
            int imageResId = itemView.getContext().getResources()
                    .getIdentifier(imageName, "drawable", itemView.getContext().getPackageName());
            if (imageResId != 0) {
                Glide.with(itemView.getContext())
                        .load(imageResId)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(recipeImage);
            } else {
                recipeImage.setImageResource(R.drawable.placeholder_image);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(recipe);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(recipe);
                }
            });
        }
    }
}
