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

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Item> itemList;

    public ReportAdapter(Context context) {
        this.context = context;
        this.itemList = new ArrayList<>();
    }

    public void setItems(List<Item> items) {
        this.itemList = items;
        notifyDataSetChanged(); // 데이터 변경 알림
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

        // 데이터 바인딩
        holder.stockName.setText(item.getStockName());
        holder.stockTitle.setText(item.getStockTitle());
        holder.bank.setText(item.getBank());
        holder.script.setText(item.getScript());

        // "요약본" 버튼 클릭 이벤트
        holder.sumButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SummaryActivity.class);
            intent.putExtra("stock_name", item.getStockName());
            intent.putExtra("stock_title", item.getStockTitle());
            intent.putExtra("bank", item.getBank());
            intent.putExtra("script", item.getScript());
            intent.putExtra("id", item.getId());
            context.startActivity(intent);
        });

        // "원문" 버튼 클릭 이벤트
        holder.oriButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("stock_name", item.getStockName());
            intent.putExtra("stock_title", item.getStockTitle());
            intent.putExtra("bank", item.getBank());
            intent.putExtra("script", item.getScript());
            intent.putExtra("id", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockTitle, bank, script;
        Button sumButton, oriButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stockName);
            stockTitle = itemView.findViewById(R.id.stockTitle);
            bank = itemView.findViewById(R.id.bank);
            script = itemView.findViewById(R.id.script);
            sumButton = itemView.findViewById(R.id.sum_button);
            oriButton = itemView.findViewById(R.id.ori_button);
        }
    }
}
