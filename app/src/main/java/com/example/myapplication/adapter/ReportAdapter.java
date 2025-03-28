package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
        holder.boxTitle.setSelected(true);
        holder.boxBank.setText(item.getBank());
        holder.boxDate.setText(item.getDate());
        holder.boxScript.setText(item.getContent());

        holder.boxButton.setOnClickListener(v -> {
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

        // 아이템이 좋아요 되어 있는지에 따라 버튼 이미지 변경
        holder.boxStarButton.setImageResource(
                item.isLiked() ? R.drawable.star_not : R.drawable.star
        );

// 버튼 클릭 시 좋아요 토글
        holder.boxStarButton.setOnClickListener(v -> {
            boolean newLikeState = !item.isLiked();
            item.setLiked(newLikeState);

            // UI 반영
            holder.boxStarButton.setImageResource(
                    newLikeState ? R.drawable.star_not : R.drawable.star
            );

            // 여기에 서버 전송 로직 넣어야 함.
        });

    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView boxName, boxTitle, boxBank, boxDate, boxScript;
        ImageButton boxStarButton;
        Button boxButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            boxName = itemView.findViewById(R.id.box_name);
            boxTitle = itemView.findViewById(R.id.box_title);
            boxDate = itemView.findViewById(R.id.box_date);
            boxScript = itemView.findViewById(R.id.box_script);
            boxButton = itemView.findViewById(R.id.box_button);
            boxStarButton = itemView.findViewById(R.id.box_star_button);


        }
    }
}
