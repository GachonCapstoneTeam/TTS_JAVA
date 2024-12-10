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
import com.example.myapplication.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private Context context;
    private List<Item> items;

    public ReportAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itembox, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Item item = items.get(position);
        holder.titleTextView.setText(item.getTitle());  // Title 매핑
        holder.stockNameTextView.setText(item.getCategory());  // 종목명 -> Category 매핑
        holder.bankTextView.setText(item.getStockName());  // 증권사
        holder.scriptTextView.setText(item.getContent());  // script

        // 버튼 클릭 이벤트 처리
        holder.summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SummaryActivity.class);
            intent.putExtra("pdfUrl", item.getPdfUrl());  // pdfUrl 전달
            context.startActivity(intent);
        });

        // ReportAdapter에서 onBindViewHolder 수정

        holder.originalButton.setOnClickListener(v -> {
            // 클릭된 아이템의 정보를 intent로 전달
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("title", item.getTitle()); // 제목
            intent.putExtra("content", item.getContent()); // 내용
            intent.putExtra("pdfUrl", item.getPdfUrl()); // PDF URL
            intent.putExtra("company", item.getStockName()); // 증권사
            intent.putExtra("category",item.getCategory()); // 종목명
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView stockNameTextView;
        TextView bankTextView;
        TextView scriptTextView;
        Button summaryButton, originalButton;

        public ReportViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.stockTitle);  // Title
            stockNameTextView = itemView.findViewById(R.id.stockName);  // 종목명
            bankTextView = itemView.findViewById(R.id.bank);  // 증권사
            scriptTextView = itemView.findViewById(R.id.script);  // script
            summaryButton = itemView.findViewById(R.id.sum_button); // 요약본 버튼
            originalButton = itemView.findViewById(R.id.ori_button); // 원문 버튼
        }
    }
}
