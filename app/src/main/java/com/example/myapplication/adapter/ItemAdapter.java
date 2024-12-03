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
import com.example.myapplication.SummaryActivity;
import com.example.myapplication.item.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context context;
    private List<Item> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onSummaryClick(int position);
        void onOriginalClick(int position);
    }

    public ItemAdapter(Context context, List<Item> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);

        holder.stockTitle.setText(item.getTitle());

        holder.summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SummaryActivity.class); // SummaryActivity로 이동
            intent.putExtra("item_title", item.getTitle()); // 데이터 전달
            context.startActivity(intent);
        });

        holder.originalButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class); // OriginalActivity로 이동
            intent.putExtra("item_title", item.getTitle()); // 데이터 전달
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stockTitle;
        Button summaryButton;
        Button originalButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockTitle = itemView.findViewById(R.id.stock_title);
            summaryButton = itemView.findViewById(R.id.summary_button);
            originalButton = itemView.findViewById(R.id.original_button);
        }
    }
}
