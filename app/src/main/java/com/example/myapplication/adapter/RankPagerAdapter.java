package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.OriginalActivity;
import com.example.myapplication.R;
import com.example.myapplication.SimplePlayerActivity;
import com.example.myapplication.entity.Item;
import com.example.myapplication.view.TTSHelper;

import java.util.List;

public class RankPagerAdapter extends RecyclerView.Adapter<RankPagerAdapter.ViewHolder> {

    private Context context;
    private List<Item> items;
    private RankPagerAdapter.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Item item); // 아이템 클릭 시 호출
    }

    public void setOnItemClickListener(RankPagerAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RankPagerAdapter(Context context, List<Item> items) {
        this.items = items;
        this.context = context;
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankPagerAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.rankTitle.setText(item.getTitle());
        holder.rankName.setText(item.getStockName());
        holder.rankViews.setText(item.getViews() + "view");
        holder.rankDate.setText(item.getDate());

        holder.itemView.setOnClickListener(v -> {
            TTSHelper ttsHelper = new TTSHelper(v.getContext());
            String fileName = item.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".mp3";

            ttsHelper.performTextToSpeech(item.getContent(), fileName, audioFile -> {
                if (audioFile != null && audioFile.exists()) {
                    Intent intent = new Intent(v.getContext(), SimplePlayerActivity.class);

                    // 리포트 정보 전달
                    intent.putExtra("Title", item.getTitle());
                    intent.putExtra("Category", item.getCategory());
                    intent.putExtra("Content", item.getContent());
                    intent.putExtra("Date", item.getDate());
                    intent.putExtra("PDF_URL", item.getPdfUrl());

                    // 오디오 파일 경로 전달
                    intent.putExtra("AudioFilePath", audioFile.getAbsolutePath());

                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), "오디오 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTitle, rankName, rankViews, rankDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTitle = itemView.findViewById(R.id.rank_title);
            rankName = itemView.findViewById(R.id.rank_name);
            rankViews = itemView.findViewById(R.id.rank_views);
            rankDate = itemView.findViewById(R.id.rank_date);
        }
    }
}