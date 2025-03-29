package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entity.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemList;
    private OnItemClickListener onItemClickListener;
    private int playingIndex = -1;

    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(Item item); // 아이템 클릭 시 호출
    }

    // 클릭 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // 생성자
    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
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

        // 데이터 바인딩
        holder.title.setText(item.getTitle()); // 제목
        holder.category.setText(item.getCategory()); // 분류
        holder.uploadTime.setText(item.getDate()); // 업로드 시간

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item); // 클릭된 아이템 전달
            }
        });

        if (position == playingIndex) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_color));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public void setPlayingIndex(int index) {
        this.playingIndex = index;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, stockName, uploadTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            // XML 레이아웃의 뷰 연결
            title = itemView.findViewById(R.id.item_title);
            category = itemView.findViewById(R.id.item_category);
            uploadTime = itemView.findViewById(R.id.item_date);
        }
    }
}
