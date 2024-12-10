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

        // 기존 데이터 설정
        holder.stockName.setText(item.getStockName());
        holder.stockTitle.setText(item.getStockTitle());
        holder.bank.setText(item.getBank());
        holder.script.setText(item.getScript());

        // 새로운 데이터 설정
        holder.date.setText(item.getDate());
        holder.views.setText("조회수: " + item.getViews());

        // 요약 버튼 클릭 이벤트
        holder.sumButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SummaryActivity.class);
            intent.putExtra("stockName", item.getStockName());
            intent.putExtra("stockTitle", item.getStockTitle());
            intent.putExtra("bank", item.getBank());
            intent.putExtra("script", item.getScript());
            intent.putExtra("views", item.getViews());
            intent.putExtra("pdf", item.getPDF_URL());
            context.startActivity(intent);
        });

        // 원문 버튼 클릭 이벤트
        holder.oriButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OriginalActivity.class);
            intent.putExtra("stockName", item.getStockName());
            intent.putExtra("stockTitle", item.getStockTitle());
            intent.putExtra("bank", item.getBank());
            intent.putExtra("script", item.getScript());
            intent.putExtra("views", item.getViews());
            intent.putExtra("pdf", item.getPDF_URL());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockTitle, bank, script, date, views;
        Button sumButton, oriButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stockName);
            stockTitle = itemView.findViewById(R.id.stockTitle);
            bank = itemView.findViewById(R.id.bank);
            script = itemView.findViewById(R.id.script);
            date = itemView.findViewById(R.id.date);
            views = itemView.findViewById(R.id.views);
            sumButton = itemView.findViewById(R.id.sum_button);
            oriButton = itemView.findViewById(R.id.ori_button);
        }
    }
}
