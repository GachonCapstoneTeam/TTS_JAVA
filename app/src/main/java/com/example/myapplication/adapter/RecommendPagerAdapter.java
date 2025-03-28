package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Item;

import java.util.List;

public class RecommendPagerAdapter extends RecyclerView.Adapter<RecommendPagerAdapter.ViewHolder> {

    private List<Item> items;

    public RecommendPagerAdapter(List<Item> items) {
        this.items = items;
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecommendPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendPagerAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.ritName.setText(item.getStockName());
        holder.ritTitle.setText(item.getTitle());
        holder.ritDate.setText(item.getDate());
        holder.ritScript.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ritName, ritTitle, ritDate, ritScript;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ritName = itemView.findViewById(R.id.rit_name);
            ritTitle = itemView.findViewById(R.id.rit_title);
            ritDate = itemView.findViewById(R.id.rit_date);
            ritScript = itemView.findViewById(R.id.rit_script);
        }
    }
}