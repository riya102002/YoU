package com.utkarsh.you;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewChatsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Userrs> userList;
    private String myprofileImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chats);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }

        Intent i = getIntent();
        String username = i.getStringExtra("Username");
        String name = i.getStringExtra("Name");
        String ID = i.getStringExtra("ID");

        TextView recivername = findViewById(R.id.recivername);
        recivername.setText("Hello " + username + "!");

        ImageView profileImage = findViewById(R.id.profileimgg);

        // Fetch profile image URL for the main user from Firebase Realtime Database
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(ID).child("profileImageUrl");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myprofileImageUrl = dataSnapshot.getValue(String.class);
                if (myprofileImageUrl != null) {
                    Glide.with(ViewChatsActivity.this)
                            .load(myprofileImageUrl)
                            .placeholder(R.drawable.youicon) // Optional: placeholder image
                            .error(R.drawable.youicon) // Optional: error image
                            .into(profileImage);
                }else{
                    Glide.with(ViewChatsActivity.this)
                            .load(myprofileImageUrl)
                            .placeholder(R.drawable.youicon) // Optional: placeholder image
                            .error(R.drawable.youicon) // Optional: error image
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize UserAdapter and userList
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        // Fetch user data from Firebase Realtime Database
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    String usernamee = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (usernamee != null && profileImageUrl != null) {
                        userList.add(new Userrs(userId, usernamee, profileImageUrl, username, ID, myprofileImageUrl));
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}
