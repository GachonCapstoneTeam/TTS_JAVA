package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.SimplePlayerActivity;
import com.example.myapplication.entity.Item;
import com.example.myapplication.view.TTSHelper;
import com.google.gson.Gson;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Item> itemList;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    public ReportAdapter(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public void setItems(List<Item> items) {
        this.itemList = items;

        // 저장된 liked 상태 불러와서 반영
        for (Item item : itemList) {
            String key = "liked_" + item.getTitle();
            item.setLiked(prefs.contains(key));
        }

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

        holder.boxcategory.setText(item.getCategory());
        holder.boxTitle.setText(item.getTitle());
        holder.boxTitle.setSelected(true);
        holder.boxDate.setText(item.getDate());
        holder.boxScript.setText(item.getContent());
        holder.boxName.setText(item.getStockName());

        // 플레이 버튼
        holder.boxButton.setOnClickListener(v -> {
            TTSHelper ttsHelper = new TTSHelper(context);
            String fileName = item.getTitle() + ".mp3";
            ttsHelper.performTextToSpeech(item.getContent(), fileName, audioFile -> {
                if (audioFile != null && audioFile.exists()) {
                    Intent intent = new Intent(context, SimplePlayerActivity.class);
                    intent.putExtra("Category", item.getCategory());
                    intent.putExtra("Title", item.getTitle());
                    intent.putExtra("Bank", item.getStockName());
                    intent.putExtra("Content", item.getContent());
                    intent.putExtra("Views", item.getViews());
                    intent.putExtra("Date", item.getDate());
                    intent.putExtra("PDF_URL", item.getPdfUrl());
                    intent.putExtra("AudioFilePath", audioFile.getAbsolutePath());

                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "오디오 파일 생성 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 초기 좋아요 버튼 상태
        holder.boxStarButton.setImageResource(
                item.isLiked() ? R.drawable.star : R.drawable.star_not
        );

        // 좋아요 버튼 클릭
        holder.boxStarButton.setOnClickListener(v -> {
            boolean newLikeState = !item.isLiked();
            item.setLiked(newLikeState);

            holder.boxStarButton.setImageResource(
                    newLikeState ? R.drawable.star : R.drawable.star_not
            );

            SharedPreferences.Editor editor = prefs.edit();
            String key = "liked_" + item.getTitle();

            if (newLikeState) {
                String json = gson.toJson(item);
                editor.putString(key, json);
            } else {
                editor.remove(key);
            }

            editor.apply();

            Intent intent = new Intent("com.example.myapplication.LIKED_UPDATED");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView boxName, boxTitle, boxDate, boxScript, boxcategory;
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
            boxcategory = itemView.findViewById(R.id.box_category);
        }
    }
}
