package com.utkarsh.you;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecommendedPostAdapter extends RecyclerView.Adapter<RecommendedPostAdapter.RecommendedViewHolder> {

    private ArrayList<RecommendedPostItem> recommendedPostItems;

    public RecommendedPostAdapter(ArrayList<RecommendedPostItem> recommendedPostItems) {
        this.recommendedPostItems = recommendedPostItems;
    }

    public static class RecommendedViewHolder extends RecyclerView.ViewHolder {
        public TextView postNameTextView;
        public TextView posttitleTextView;
        public TextView postdescriptionTextView;
        public TextView postAuthorTextView;
        public ImageView postimageView;

        public RecommendedViewHolder(@NonNull View itemView) {
            super(itemView);
            postNameTextView = itemView.findViewById(R.id.item_postName);
            posttitleTextView = itemView.findViewById(R.id.item_post_title);
            postdescriptionTextView = itemView.findViewById(R.id.item_post_description);
            postimageView = itemView.findViewById(R.id.item_post_image);
            postAuthorTextView = itemView.findViewById(R.id.item_postAuthor);
        }
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_post_item, parent, false);
        return new RecommendedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedViewHolder holder, int position) {
        RecommendedPostItem currentItem = recommendedPostItems.get(position);
        holder.postNameTextView.setText("Post ID: " + currentItem.getPostName());
        holder.posttitleTextView.setText(currentItem.getPosttitle());
        holder.postdescriptionTextView.setText(currentItem.getPostdescription());
        holder.postAuthorTextView.setText("Post By: " + currentItem.getAuthorName());
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getPostimageResource()) // Assuming getPostImageUrl() returns the image URL string
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
                .centerCrop() // Image scale type
                .placeholder(R.drawable.imageplaceholder) // Placeholder image while loading
                .error(R.drawable.imageplaceholder) // Error image if loading fails
                .into(holder.postimageView);    }

    @Override
    public int getItemCount() {
        return recommendedPostItems.size();
    }
}
