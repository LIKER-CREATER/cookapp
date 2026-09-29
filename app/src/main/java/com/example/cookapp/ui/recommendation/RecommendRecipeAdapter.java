package com.example.cookapp.ui.recommendation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookapp.R;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.util.RecipeImageLoader;

public class RecommendRecipeAdapter extends ListAdapter<RecommendationService.RecipeWithReason, RecommendRecipeAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
    }

    private final OnItemClickListener listener;

    private static final DiffUtil.ItemCallback<RecommendationService.RecipeWithReason> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RecommendationService.RecipeWithReason>() {
                @Override
                public boolean areItemsTheSame(@NonNull RecommendationService.RecipeWithReason oldItem,
                                               @NonNull RecommendationService.RecipeWithReason newItem) {
                    return oldItem.recipe.getId() == newItem.recipe.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RecommendationService.RecipeWithReason oldItem,
                                                  @NonNull RecommendationService.RecipeWithReason newItem) {
                    return oldItem.score == newItem.score
                            && oldItem.recipe.getName().equals(newItem.recipe.getName());
                }
            };

    public RecommendRecipeAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommend_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendationService.RecipeWithReason item = getItem(position);
        holder.bind(item, item.recipe, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImage;
        private final TextView tvReason;
        private final TextView tvTime;
        private final TextView tvRecipeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
        }

        public void bind(RecommendationService.RecipeWithReason data, Recipe recipe, OnItemClickListener clickListener) {
            tvRecipeName.setText(recipe.getName());
            tvReason.setText(data.reason);
            tvTime.setText(recipe.getCookingTime() + "分钟");

            String imageName = recipe.getImageName();
            RecipeImageLoader.load(itemView.getContext(), recipeImage, imageName);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(recipe);
                }
            });
        }
    }
}
