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
import com.example.myapplication.view.Item;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Item> itemList;

    public ReportAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<Item> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itembox, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.boxName.setText(item.getCategory());
        holder.boxTitle.setText(item.getTitle());
        holder.boxTitle.setSelected(true); // 마키 효과를 위한 선택 상태 설정
        holder.boxBank.setText(item.getBank());
        holder.boxDate.setText(item.getDate());
        holder.boxScript.setText(item.getContent());

        // Original Button Click Listener
        holder.boxOriButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("Category", item.getCategory());
            intent.putExtra("Title", item.getTitle());
            intent.putExtra("Bank", item.getBank());
            intent.putExtra("PDF Content", item.getPdfcontent());
            intent.putExtra("Views", item.getViews());
            intent.putExtra("Date", item.getDate());
            intent.putExtra("PDF_URL", item.getPdfUrl());
            context.startActivity(intent);
        });

        // Summary Button Click Listener
        holder.boxSumButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("Category", item.getCategory());
            intent.putExtra("Title", item.getTitle());
            intent.putExtra("Bank", item.getBank());
            intent.putExtra("Content", item.getContent());
            intent.putExtra("Views", item.getViews());
            intent.putExtra("Date", item.getDate());
            intent.putExtra("PDF_URL", item.getPdfUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView boxName, boxTitle, boxBank, boxDate, boxScript;
        Button boxOriButton, boxSumButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            boxName = itemView.findViewById(R.id.box_name);
            boxTitle = itemView.findViewById(R.id.box_title);
            boxBank = itemView.findViewById(R.id.box_bank);
            boxDate = itemView.findViewById(R.id.box_date);
            boxScript = itemView.findViewById(R.id.box_script);
            boxOriButton = itemView.findViewById(R.id.box_ori_button);
            boxSumButton = itemView.findViewById(R.id.box_sum_button);
        }
    }
}
