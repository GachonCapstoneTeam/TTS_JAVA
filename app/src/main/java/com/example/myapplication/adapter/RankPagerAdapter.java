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

public class RankPagerAdapter extends RecyclerView.Adapter<RankPagerAdapter.ViewHolder> {

    private List<Item> items;

    public RankPagerAdapter(List<Item> items) {
        this.items = items;
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankPagerAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.rankTitle.setText(item.getTitle());
        holder.rankBank.setText(item.getStockName());
        holder.rankViews.setText(item.getViews() + "회");
        holder.rankDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTitle, rankBank, rankViews, rankDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTitle = itemView.findViewById(R.id.rank_title);
            rankBank = itemView.findViewById(R.id.rank_bank);
            rankViews = itemView.findViewById(R.id.rank_views);
            rankDate = itemView.findViewById(R.id.rank_date);
        }
    }
}
