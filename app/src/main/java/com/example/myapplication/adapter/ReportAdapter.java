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

        holder.stockName.setText(item.getCategory());
        holder.stockTitle.setText(item.getTitle());
        holder.stockTitle.setSelected(true); // 마키 효과를 위한 선택 상태 설정
        holder.bank.setText(item.getBank());
        holder.date.setText(item.getDate());
        holder.views.setText("조회수 : " +String.valueOf(item.getViews()));
        holder.script.setText(item.getContent());

        //original button
        holder.oriButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("Category", item.getCategory());
            intent.putExtra("Title", item.getTitle());
            intent.putExtra("Bank", item.getBank());
            intent.putExtra("PDF Content", item.getPdfcontent()); //이 부분 본문 정보로 바꿔야함.
            intent.putExtra("Views", item.getViews());
            intent.putExtra("Date", item.getDate());
            intent.putExtra("PDF_URL", item.getPdfUrl());
            context.startActivity(intent);
        });

        //summary button
        holder.sumButton.setOnClickListener(v -> {
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
        TextView stockName, stockTitle, bank, date, views, script;
        Button oriButton, sumButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stockName);
            stockTitle = itemView.findViewById(R.id.stockTitle);
            bank = itemView.findViewById(R.id.bank);
            date = itemView.findViewById(R.id.date);
            views = itemView.findViewById(R.id.views);
            script = itemView.findViewById(R.id.script);
            oriButton = itemView.findViewById(R.id.ori_button);
            sumButton = itemView.findViewById(R.id.sum_button);
        }
    }
}
