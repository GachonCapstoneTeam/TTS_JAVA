package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class ShimmerAdapter extends RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder> {

    private final int shimmerItemCount;

    public ShimmerAdapter(int itemCount) {
        this.shimmerItemCount = itemCount; // 몇 개의 스켈레톤 아이템을 보여줄지
    }

    @NonNull
    @Override
    public ShimmerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_skeleton, parent, false);
        return new ShimmerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShimmerViewHolder holder, int position) {
        // 아무 데이터 바인딩 없이 스켈레톤 뷰만 보여줌
    }

    @Override
    public int getItemCount() {
        return shimmerItemCount;
    }

    public static class ShimmerViewHolder extends RecyclerView.ViewHolder {
        public ShimmerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
