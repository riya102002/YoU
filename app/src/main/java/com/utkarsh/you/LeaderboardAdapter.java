package com.utkarsh.you;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private List<Post> topPosts;
    private Context context;

    public LeaderboardAdapter(List<Post> topPosts, Context context) {
        this.topPosts = topPosts;
        this.context = context;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_card, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        Post post = topPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return topPosts.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView likesTextView;
        TextView captionTextView;
        TextView descriptionTextView;
        ImageView postImageView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textUsername);
            likesTextView = itemView.findViewById(R.id.textLikes);
            captionTextView = itemView.findViewById(R.id.textCaption);
            descriptionTextView = itemView.findViewById(R.id.textDescription);
            postImageView = itemView.findViewById(R.id.imagePost);
        }

        public void bind(Post post) {
            usernameTextView.setText(post.getPostAuthor());
            likesTextView.setText("Likes: " + post.getLikeCount());
            captionTextView.setText(post.getPostCaption());
            descriptionTextView.setText(post.getPostDescription());
            Glide.with(itemView.getContext()).load(post.getProfileImageUrl()).into(postImageView);
        }
    }
}