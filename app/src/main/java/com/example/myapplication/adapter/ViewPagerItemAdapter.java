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

    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewPagerItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itembox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Item item = items.get(position);
        holder.itemTitle.setText(item.getTitle());
        holder.itemCategory.setText(item.getCategory());
        holder.itemDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemCategory, itemDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.box_title);
            itemCategory = itemView.findViewById(R.id.box_name);
            itemDate = itemView.findViewById(R.id.box_date);
        }
    }
}
