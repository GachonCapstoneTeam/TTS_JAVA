package com.example.myapplication.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.HomeFragment;
import com.example.myapplication.MainFragment;
import com.example.myapplication.RecommendFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MainFragment(); // Search
            case 1:
                return new HomeFragment(); // Home
            case 2:
                return new RecommendFragment(); //Recommand
            default:
                return new HomeFragment(); // Default HomeFragment
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 총 Fragment 수
    }
}
