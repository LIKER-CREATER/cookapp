package com.example.cookapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ShoppingListPagerAdapter extends FragmentStateAdapter {
    public ShoppingListPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ShoppingListTabFragment.newInstance(position == 1);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
} 