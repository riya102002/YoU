package com.utkarsh.you;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.RecommendedViewHolder> {

    private ArrayList<RecommendedItem> recommendedItems;

    public RecommendedAdapter(ArrayList<RecommendedItem> recommendedItems) {
        this.recommendedItems = recommendedItems;
    }

    public static class RecommendedViewHolder extends RecyclerView.ViewHolder {
        public TextView docNameTextView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView imageView;
        public TextView courseType;

        public RecommendedViewHolder(@NonNull View itemView) {
            super(itemView);
            docNameTextView = itemView.findViewById(R.id.item_docName);
            titleTextView = itemView.findViewById(R.id.item_title);
            descriptionTextView = itemView.findViewById(R.id.item_description);
            imageView = itemView.findViewById(R.id.item_image);
            courseType = itemView.findViewById(R.id.courseType);
        }
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_item, parent, false);
        return new RecommendedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedViewHolder holder, int position) {
        RecommendedItem currentItem = recommendedItems.get(position);
        holder.docNameTextView.setText(currentItem.getDocumentName());
        holder.titleTextView.setText(currentItem.getTitle());
        holder.descriptionTextView.setText(currentItem.getDescription());
        holder.imageView.setImageResource(currentItem.getImageResource());
        holder.courseType.setText("Course Type: "+currentItem.getCourseType());
    }

    @Override
    public int getItemCount() {
        return recommendedItems.size();
    }
}
