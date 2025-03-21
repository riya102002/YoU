// ActivityFragment.java
package com.utkarsh.you;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<ActivityItem> activityList;

    private DatabaseReference databaseReference;
    String userId, name, username;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Activities");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity, container, false);

        // Retrieve user information from arguments
        userId = getArguments().getString("ID");
        name = getArguments().getString("Name");
        username = getArguments().getString("Username");

        // Initialize RecyclerView and adapter
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(getContext(), activityList);
        recyclerView.setAdapter(adapter);

        // Initialize empty box image
        ImageView emptyBoxImage = rootView.findViewById(R.id.empty_box_image);

        // Fetch activity data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activityList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        for(DataSnapshot childChildSnapshot : childSnapshot.getChildren()){
                            String itemName = childChildSnapshot.getKey();
                            String itemId = snapshot.getKey();
                            String userName = childSnapshot.getKey();
                            String message;
                            if (itemId.endsWith("PostUpload")) {
                                message = "New Post \uD83D\uDD14 from " + userName;
                            } else {
                                message = "New Document \uD83D\uDCDA from " + userName;
                            }
                            activityList.add(new ActivityItem(userName, message, itemId, itemName, userId));
                        }
                    }
                }

                if (activityList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyBoxImage.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyBoxImage.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        return rootView;
    }
}