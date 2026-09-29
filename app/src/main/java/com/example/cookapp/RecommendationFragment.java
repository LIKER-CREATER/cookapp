package com.example.cookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookapp.database.AppDatabase;
import com.example.cookapp.database.Recipe;
import com.example.cookapp.database.User;
import com.example.cookapp.ui.recommendation.RecommendRecipeAdapter;
import com.example.cookapp.ui.recommendation.RecommendationService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecommendationFragment extends Fragment implements RecommendRecipeAdapter.OnItemClickListener {

    private static final int PICKS_COUNT = 4;
    private static final int SPAN_COUNT = 2;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ExecutorService executorService;
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    // 全量缓存（首次加载后不变）
    private List<RecommendationService.RecipeWithReason> cachedAll = Collections.emptyList();
    // 当前筛选结果（菜系切换时重新计算，同时驱动精选和列表）
    private List<RecommendationService.RecipeWithReason> currentFiltered = Collections.emptyList();
    private Recipe.CuisineType currentCuisine = null;

    private RecyclerView rvAllRecommendations;
    private ChipGroup chipGroup;
    private TextView tvListTitle;
    private LinearLayout emptyView;
    private RecommendationAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAllRecommendations = view.findViewById(R.id.rvAllRecommendations);
        chipGroup = view.findViewById(R.id.chipGroup);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        emptyView = view.findViewById(R.id.emptyView);

        // 双列网格
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? SPAN_COUNT : 1;
            }
        });
        rvAllRecommendations.setLayoutManager(gridLayoutManager);
        rvAllRecommendations.setNestedScrollingEnabled(false);

        // 创建包装 adapter（在 loadRecommendations 之前，以便 HeaderViewHolder 初始化时引用）
        adapter = new RecommendationAdapter();
        rvAllRecommendations.setAdapter(adapter);

        setupChips();
        loadRecommendations();
    }

    /**
     * 统一管理推荐内容：
     * 1. 全量加载 → cachedAll（仅一次）
     * 2. 菜系切换 → 过滤 cachedAll → currentFiltered
     * 3. currentFiltered 同时驱动精选（取前 PICKS_COUNT）和下方列表
     *
     * 不再依赖 ListAdapter 的 submitList + notifyItemRangeChanged 组合，避免异步冲突。
     */
    private void loadRecommendations() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            User currentUser = db.userDao().getCurrentUser();
            long userId = currentUser != null ? currentUser.getId() : 1L;

            RecommendationService service = new RecommendationService(
                    db.recipeDao(), db.cookingHistoryDao(), userId);
            List<RecommendationService.RecipeWithReason> all = service.getRecommendations();

            cachedAll = new ArrayList<>(all);

            requireActivity().runOnUiThread(() -> {
                if (destroyed.get()) return;
                applyFilter(); // 首次加载使用默认全部筛选
            });
        });
    }

    private void applyFilter() {
        // 统一从全量缓存中按菜系过滤
        currentFiltered = RecommendationService.filterByCuisine(cachedAll, currentCuisine);

        // 通知 adapter 整体刷新（header + 列表都基于 currentFiltered）
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // 空状态控制
        boolean isEmpty = currentFiltered.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        tvListTitle.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * 单一 adapter，同时负责：
     * - position=0：今日精选 header（横向精选列表，取 currentFiltered 前 PICKS_COUNT 条）
     * - position>0：下方双列菜谱卡片（currentFiltered 除精选外的剩余部分）
     *
     * 数据源统一由 Fragment 的 currentFiltered 提供，onBind 时直接读取，
     * 不再出现 ListAdapter 内部列表与外部刷新时序不匹配导致的越界问题。
     */
    private class RecommendationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final RecommendRecipeAdapter.OnItemClickListener clickListener =
                RecommendationFragment.this;

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            // position=0 → header
            // position=1..PICKS_COUNT → 精选卡片（最多 PICKS_COUNT 条）
            // position>PICKS_COUNT → 剩余全部推荐
            // 总数 = 1(header) + currentFiltered.size()
            return 1 + currentFiltered.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_recommendation_header, parent, false);
                return new HeaderViewHolder(v);
            }
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommend_recipe, parent, false);
            return new RecommendRecipeAdapter.ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                // position=0：今日精选，取前 PICKS_COUNT 条
                List<RecommendationService.RecipeWithReason> picks =
                        currentFiltered.isEmpty()
                                ? Collections.emptyList()
                                : currentFiltered.subList(0, Math.min(PICKS_COUNT, currentFiltered.size()));
                ((HeaderViewHolder) holder).bind(picks, clickListener);
            } else {
                // position>0：对应 currentFiltered[position - 1]
                int itemPos = position - 1;
                if (itemPos >= 0 && itemPos < currentFiltered.size()) {
                    RecommendationService.RecipeWithReason item = currentFiltered.get(itemPos);
                    RecommendRecipeAdapter.ViewHolder vh =
                            (RecommendRecipeAdapter.ViewHolder) holder;
                    vh.bind(item, item.recipe, clickListener);
                }
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            private final RecyclerView rvPicks;

            HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                rvPicks = itemView.findViewById(R.id.rvPicks);
                rvPicks.setLayoutManager(new LinearLayoutManager(
                        itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvPicks.setNestedScrollingEnabled(false);
                rvPicks.setAdapter(new RecommendRecipeAdapter(clickListener));
            }

            void bind(List<RecommendationService.RecipeWithReason> picks,
                      RecommendRecipeAdapter.OnItemClickListener listener) {
                ((RecommendRecipeAdapter) rvPicks.getAdapter())
                        .submitList(new ArrayList<>(picks));
            }
        }
    }

    private void setupChips() {
        Recipe.CuisineType[] cuisines = {
                null,
                Recipe.CuisineType.SICHUAN,
                Recipe.CuisineType.CANTONESE,
                Recipe.CuisineType.SHANDONG,
                Recipe.CuisineType.JIANGSU,
                Recipe.CuisineType.ZHEJIANG,
                Recipe.CuisineType.HUNAN,
        };
        String[] labels = {
                "全部", "川菜", "粤菜", "鲁菜", "苏菜", "浙菜", "湘菜"
        };

        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            final Recipe.CuisineType cuisine = cuisines[i];
            chip.setOnClickListener(v -> {
                if (currentCuisine != cuisine) {
                    currentCuisine = cuisine;
                    applyFilter();
                }
            });
            chipGroup.addView(chip);
        }
    }

    @Override
    public void onItemClick(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed.set(true);
        executorService.shutdown();
    }
}
