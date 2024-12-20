package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.OriginalActivity;
import com.example.myapplication.R;
import com.example.myapplication.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemList;

    public ItemAdapter(Context context) {
        this.context = context;
        this.itemList = new ArrayList<>();
    }

    public void setItems(List<Item> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.stockTitle.setText(item.getTitle()); // 제목
        holder.serialNumber.setText("증권사: " + item.getStockName()); // 증권사

        holder.summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("content", item.getContent());
            intent.putExtra("company", item.getStockName());
            intent.putExtra("pdfUrl", item.getPdfUrl());
            context.startActivity(intent);
        });

        holder.originalButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("pdfUrl", item.getPdfUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView stockTitle, serialNumber;
        Button summaryButton, originalButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            stockTitle = itemView.findViewById(R.id.stock_title);
            serialNumber = itemView.findViewById(R.id.serial_number);
            summaryButton = itemView.findViewById(R.id.summary_button);
            originalButton = itemView.findViewById(R.id.original_button);
        }
    }
}