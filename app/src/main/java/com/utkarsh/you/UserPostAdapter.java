//package com.utkarsh.you;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.util.List;
//
//public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder> {
//
//    private List<UserPost> userPostList;
//
//    public UserPostAdapter(List<UserPost> userPostList) {
//        this.userPostList = userPostList;
//    }
//
//    @NonNull
//    @Override
//    public UserPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_post, parent, false);
//        return new UserPostViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull UserPostViewHolder holder, int position) {
//        UserPost userPost = userPostList.get(position);
//        holder.captionTextView.setText(userPost.getCaption());
//        holder.descriptionTextView.setText(userPost.getDescription());
//        holder.authorPostTextView.setText(userPost.getAuthor());
//        holder.keywordsTextView.setText(userPost.getKeywords());
//        // Load image using Glide
//        Glide.with(holder.itemView.getContext())
//                .load(userPost.getImageUrl())
//                .into(holder.postImg);
//        // Set other views with post data
//    }
//
//    @Override
//    public int getItemCount() {
//        return userPostList.size();
//    }
//
//    public static class UserPostViewHolder extends RecyclerView.ViewHolder {
//        TextView captionTextView;
//        TextView descriptionTextView;
//        TextView authorPostTextView;
//        ImageView postImg;
//        TextView keywordsTextView;
//        // Other views for post data
//
//        public UserPostViewHolder(@NonNull View itemView) {
//            super(itemView);
//            captionTextView = itemView.findViewById(R.id.captionTextView);
//            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
//            authorPostTextView = itemView.findViewById(R.id.authorPostTextView);
//            postImg = itemView.findViewById(R.id.profilePost);
//            keywordsTextView = itemView.findViewById(R.id.keywordsTextView);
//            // Initialize other views
//        }
//    }
//}
