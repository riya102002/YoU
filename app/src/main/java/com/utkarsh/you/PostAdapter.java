package com.utkarsh.you;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList, Context context){
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostAdapter.PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId());
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likeCount = (int) snapshot.getChildrenCount();
                holder.likeCount.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Set click listener for opening user profile
        holder.openProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewUserProfileActivity.class);
                intent.putExtra("authorId", post.getAuthorIdd()); // Pass the user ID to the profile activity
                intent.putExtra("currentUser", post.getCurrentUser());
                intent.putExtra("authorUsername", post.getPostAuthor());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titlePostTextView;
        TextView descriptionPostTextView;
        TextView authorPostTextView;
        TextView captionPostTextView;
        ImageView profilePost;
        ImageView profileAuthor;
        ImageView heartIcon;
        TextView likeCount;
        TextView openProfileTextView; // Added TextView for opening profile

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titlePostTextView = itemView.findViewById(R.id.titlePostTextView);
            descriptionPostTextView = itemView.findViewById(R.id.descriptionPostTextView);
            authorPostTextView = itemView.findViewById(R.id.authorPostTextView);
            captionPostTextView = itemView.findViewById(R.id.captionPostTextView);
            profilePost = itemView.findViewById(R.id.profilePost);
            profileAuthor = itemView.findViewById(R.id.profileAuthor);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            openProfileTextView = itemView.findViewById(R.id.openProfileTextView);
        }

        public void bind(Post post) {
            titlePostTextView.setText(post.getKeywords());
            descriptionPostTextView.setText(post.getPostDescription());
            authorPostTextView.setText("@" + post.getPostAuthor());
            captionPostTextView.setText(post.getPostCaption());
            Glide.with(itemView.getContext()).load(post.getProfileImageUrl()).into(profilePost);
            Glide.with(itemView.getContext()).load(post.getAuthorId()).into(profileAuthor);

            // Load the heart icon based on whether the post is liked by the current user
            DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference()
                    .child("Likes").child(post.getPostId()).child(post.getCurrentUser());
            likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        heartIcon.setImageResource(R.drawable.hearticonliked);
                    } else {
                        heartIcon.setImageResource(R.drawable.hearticonnotliked);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });

            // Click listener for the heart icon
            heartIcon.setOnClickListener(view -> {
                DatabaseReference postLikesRef = FirebaseDatabase.getInstance().getReference()
                        .child("Likes").child(post.getPostId()).child(post.getCurrentUser());
                postLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Remove like
                            postLikesRef.removeValue();
                            heartIcon.setImageResource(R.drawable.hearticonnotliked);
                        } else {
                            // Add like
                            postLikesRef.setValue(true);
                            heartIcon.setImageResource(R.drawable.hearticonliked);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            });

            // Real-time listener for changes in like count
            DatabaseReference likeCountRef = FirebaseDatabase.getInstance().getReference()
                    .child("Likes").child(post.getPostId());
            likeCountRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Update like count UI
                    long likeCountValue = snapshot.getChildrenCount();
                    likeCount.setText(String.valueOf(likeCountValue));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }
}

class Post {
    private String postId;
    private String authorId;
    private String postDescription;
    private String postAuthor;
    private String postCaption;
//    private String size;
    private String profileImageUrl;
//    private String userId;
    private String keywords;
    private String currentUser;
    private String authorIdd;
    private int likeCount; // New field to store like count

    private boolean likedByCurrentUser; // Flag to store whether the post is liked by the current user

    public Post(String postId, String postAuthor, String postCaption, String postDescription, String keywords, String profileImageUrl, String authorId, String currentUser, String authorIdd) {
        this.postId = postId;
        this.authorId = authorId;
        this.postDescription = postDescription;
        this.postAuthor = postAuthor;
        this.postCaption = postCaption;
//        this.time = time;
        this.profileImageUrl = profileImageUrl;
//        this.userId = userId;
        this.keywords = keywords;
        this.currentUser = currentUser;
        this.authorIdd = authorIdd;
        this.likeCount = 0;
        this.likedByCurrentUser = false; // Initially assume not liked by current user
        // Check if the post is liked by the current user
        checkIfLikedByCurrentUser();
    }

    public String getAuthorIdd(){ return authorIdd;}

    public String getPostId(){
        return postId;
    }

    // Method to check if the current user has liked this post
    public void checkIfLikedByCurrentUser() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).child(currentUser);
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likedByCurrentUser = snapshot.exists(); // Update the flag based on whether the snapshot exists
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public void fetchLikeCount() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likeCount = (int) snapshot.getChildrenCount(); // Update like count
                // Notify any listeners about the change
                // You can implement this if you have custom listeners for the like count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }


    public String getAuthorId() {
        return authorId;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public String getPostAuthor() {
        return postAuthor;
    }

    public String getPostCaption(){ return postCaption;}

    public String getProfileImageUrl(){ return profileImageUrl;}

    public String getKeywords(){return keywords;}

    public String getCurrentUser(){return currentUser;}

}
