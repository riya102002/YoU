package com.utkarsh.you;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfileActivity extends AppCompatActivity {

    private GridView gridView;
    private UserPostGridAdapter userPostGridAdapter;
    private List<UserPost> userPostList;
    private TextView username, authorPostCount, authorDocCount, postsfromLabel;
    private String usernamee;
    private CircleImageView profileImageView;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        postsfromLabel = findViewById(R.id.postsfromLabel);
        username = findViewById(R.id.authorName);
        authorPostCount = findViewById(R.id.authorPostCount);
        authorDocCount = findViewById(R.id.authorUploadCount);
        profileImageView = findViewById(R.id.profileImageView);

        gridView = findViewById(R.id.gridProfilePostView);
        gridView.setNumColumns(2); // Set number of columns to 2
        userPostList = new ArrayList<>();
        userPostGridAdapter = new UserPostGridAdapter(userPostList);
        gridView.setAdapter(userPostGridAdapter);

        // Getting the authorId from Intent
        String authorId = getIntent().getStringExtra("authorId");
        usernamee = getIntent().getStringExtra("authorUsername");

        // Retrieving profile image URL and counts from Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(authorId);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    username.setText("@" + usernamee);
                    postsfromLabel.setText("Posts from " + usernamee);
                    Glide.with(ViewUserProfileActivity.this).load(profileImageUrl).placeholder(R.drawable.youicon).into(profileImageView);

                    // Count the number of posts
                    DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(usernamee);
                    postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                            long postCount = postsSnapshot.getChildrenCount();
                            authorPostCount.setText(String.valueOf(postCount));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(ViewUserProfileActivity.this, "Failed to load post count: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Count the number of documents
                    DatabaseReference docRef = FirebaseDatabase.getInstance().getReference().child("Documents").child(dataSnapshot.child("username").getValue(String.class));
                    docRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot docSnapshot) {
                            long docCount = docSnapshot.getChildrenCount();
                            authorDocCount.setText(String.valueOf(docCount));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(ViewUserProfileActivity.this, "Failed to load document count: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Retrieve posts and add profile image URL to each post
                    retrievePosts(usernamee, profileImageUrl);
                } else {
                    // Handle case when profile image URL doesn't exist
                    Toast.makeText(ViewUserProfileActivity.this, "Profile image not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Toast.makeText(ViewUserProfileActivity.this, "Failed to load profile image: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrievePosts(String usernamee, String profileImageUrl) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(usernamee);
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPostList.clear(); // Clear previous data
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserPost userPost = postSnapshot.getValue(UserPost.class);
                    userPost.setProfileImageUrl(profileImageUrl); // Set profile image URL
                    userPostList.add(userPost);
                }
                userPostGridAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(ViewUserProfileActivity.this, "Failed to load posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
