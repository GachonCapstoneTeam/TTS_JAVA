package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Item;

import java.util.List;

public class ViewPagerItemAdapter extends RecyclerView.Adapter<ViewPagerItemAdapter.ViewHolder> {

    private final Context context;
    private final List<Item> items;

    public ViewPagerItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(params);
        }

        Item item = items.get(position);
        holder.itemTitle.setText(item.getTitle());
        holder.itemCategory.setText(item.getCategory());
        holder.stockName.setText(item.getStockName());
        holder.itemDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemCategory, stockName, itemDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemCategory = itemView.findViewById(R.id.item_category);
            stockName = itemView.findViewById(R.id.stockName);
            itemDate = itemView.findViewById(R.id.item_date);
        }
    }
}
