package com.utkarsh.you;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAllPostActivity extends AppCompatActivity {

    private PostAdapter postAdapter;
    String username, name, id;
    private RecyclerView recyclerPostView;
    private List<Post> postList;
    private DatabaseReference databaseReference, databaseReference1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }
        setContentView(R.layout.activity_view_all_post);

        Intent i = getIntent();
        username = i.getStringExtra("Username");
        name = i.getStringExtra("Name");
        id = i.getStringExtra("ID");

        ImageView filterIcon = findViewById(R.id.filter_icon);
        filterIcon.setOnClickListener(v -> showFilterDialog());

        ImageView leaderBoardIcon = findViewById(R.id.leaderBoard);
        leaderBoardIcon.setOnClickListener(v -> {
            getTop3PostsByLikes();
        });

        recyclerPostView = findViewById(R.id.recyclerPostView);
        recyclerPostView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        databaseReference1 = FirebaseDatabase.getInstance().getReference().child("users");

        retrieveDataFromDatabase();
    }

    private void getTop3PostsByLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> postLikesMap = new HashMap<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    int likeCount = (int) postSnapshot.getChildrenCount();
                    postLikesMap.put(postId, likeCount);
                }

                List<Post> sortedPosts = new ArrayList<>(postList);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(sortedPosts, (p1, p2) -> Integer.compare(postLikesMap.getOrDefault(p2.getPostId(), 0),
                            postLikesMap.getOrDefault(p1.getPostId(), 0)));
                }

                List<Post> top3Posts = new ArrayList<>();
                for (int i = 0; i < Math.min(sortedPosts.size(), 3); i++) {
                    Post post = sortedPosts.get(i);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        post.setLikeCount(postLikesMap.getOrDefault(post.getPostId(), 0)); // Update like count for each post
                    }
                    top3Posts.add(post);
                }

                showLeaderboardDialog(new LeaderboardAdapter(top3Posts, ViewAllPostActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any database error
            }
        });
    }

    private void showLeaderboardDialog(LeaderboardAdapter leaderboardAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAllPostActivity.this);
        builder.setTitle("Leaderboard");

        // Create the RecyclerView programmatically
        RecyclerView recyclerView = new RecyclerView(ViewAllPostActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewAllPostActivity.this));
        recyclerView.setAdapter(leaderboardAdapter);

        // Set the RecyclerView to the dialog body
        builder.setView(recyclerView);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Username");

        View view = getLayoutInflater().inflate(R.layout.dialog_filter_posts, null);
        Spinner spinner = view.findViewById(R.id.spinner_usernames);

        // Add "All" option to the spinner along with the existing usernames
        List<String> allUsernames = new ArrayList<>();
        allUsernames.add("All");
        allUsernames.addAll(getUsernames());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allUsernames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        builder.setView(view);
        builder.setPositiveButton("Filter", (dialog, which) -> {
            String selectedUsername = spinner.getSelectedItem().toString();
            if (selectedUsername.equals("All")) {
                displayAllPosts();
            } else {
                filterDataByUsername(selectedUsername);
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void displayAllPosts() {
        postAdapter = new PostAdapter(postList, ViewAllPostActivity.this);
        recyclerPostView.setAdapter(postAdapter);
        postAdapter.notifyDataSetChanged();
    }

    private List<String> getUsernames() {
        List<String> usernames = new ArrayList<>();
        // Add usernames data here from your existing dataset or Firebase data
        for (Post post : postList) {
            if (!usernames.contains(post.getPostAuthor())) {
                usernames.add(post.getPostAuthor());
            }
        }
        return usernames;
    }

    private void filterDataByUsername(String selectedUsername) {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : postList) {
            if (post.getPostAuthor().equals(selectedUsername)) {
                filteredList.add(post);
            }
        }

        postAdapter = new PostAdapter(filteredList, ViewAllPostActivity.this);
        recyclerPostView.setAdapter(postAdapter);
        postAdapter.notifyDataSetChanged();
    }

    private void retrieveDataFromDatabase(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    String postId = snapshot1.getKey();
//                    String postTitle = snapshot1.child("author").getValue(String.class);
                    String authorTitle = snapshot1.child("author").getValue(String.class);
                    String caption = snapshot1.child("caption").getValue(String.class);
                    String keywords = snapshot1.child("keywords").getValue(String.class);
                    String imageUrl = snapshot1.child("imageUrl").getValue(String.class);
                    String description = snapshot1.child("description").getValue(String.class);
                    String authorId = snapshot1.child("authorId").getValue(String.class);

                    // Retrieve profileImageUrl from users node
                    databaseReference1.child(authorId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String.class);
                                // Add book to list
                                postList.add(new Post(postId, authorTitle, caption, description, keywords, imageUrl, profileImageUrl, id, authorId));
                                postAdapter.notifyDataSetChanged(); // Call notifyDataSetChanged() after setting the adapter
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                        }
                    });
                }
                // Move the adapter initialization and setting outside the loop
                postAdapter = new PostAdapter(postList, ViewAllPostActivity.this);
                recyclerPostView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}