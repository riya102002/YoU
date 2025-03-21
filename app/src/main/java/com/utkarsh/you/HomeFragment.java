package com.utkarsh.you;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    FloatingActionButton mAddPostFab, mAddPaperFab;
    ExtendedFloatingActionButton mAddFab;
    TextView addPostActionText, addPaperActionText;
    TextView greetingText, viewall, viewallpost; // Added TextView for displaying greeting
    RecyclerView recommendedList, recommendedPostList;
    RecommendedAdapter recommendedAdapter;
    RecommendedPostAdapter recommendedPostAdapter;
    Boolean isAllFabsVisible;
    ImageView chat;

    private ViewPager2 imageSlider;
    private ArrayList<Integer> imageList;
    private ImageSliderAdapter imageSliderAdapter;
    private DatabaseReference databaseReference, databaseReferencePost;
    String userId, name, username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        recommendedList = rootView.findViewById(R.id.recommended_list);
        recommendedPostList = rootView.findViewById(R.id.recommended_post_list);

        recommendedList.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendedPostList.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseReference = FirebaseDatabase.getInstance().getReference().child("latestDoc");
        databaseReferencePost = FirebaseDatabase.getInstance().getReference().child("latestPosts");

        // Sample data for the recommended list
        ArrayList<RecommendedItem> recommendedItems = new ArrayList<>();
        ArrayList<RecommendedPostItem> recommendedPostItems = new ArrayList<>();

        // Initialize and set adapter for RecyclerView
        recommendedAdapter = new RecommendedAdapter(recommendedItems);
        recommendedList.setAdapter(recommendedAdapter);

        recommendedPostAdapter = new RecommendedPostAdapter(recommendedPostItems);
        recommendedPostList.setAdapter(recommendedPostAdapter);

        // Retrieve user information from arguments
        userId = getArguments().getString("ID");
        name = getArguments().getString("Name");
        username = getArguments().getString("Username");

        imageList = new ArrayList<>();
        imageList.add(R.mipmap.p1);
        imageList.add(R.mipmap.p2);
        imageList.add(R.mipmap.p3);

        imageSlider = rootView.findViewById(R.id.imageSlider);
        imageSliderAdapter = new ImageSliderAdapter(new int[] {
                R.mipmap.p1, R.mipmap.p2, R.mipmap.p3
        });
        imageSlider.setAdapter(imageSliderAdapter);
        imageSliderAdapter.setImageResources(R.mipmap.p1, R.mipmap.p2, R.mipmap.p3);
        imageSliderAdapter.setOnImageClickListener(new ImageSliderAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                handleImageClick(position);
            }
        });
        startAutoImageSlider();

        // Display greeting text at the top
        greetingText = rootView.findViewById(R.id.greeting_text);
        greetingText.setText("Hi, " + name +"! 👋");

        mAddFab = rootView.findViewById(R.id.add_fab);
        mAddPostFab = rootView.findViewById(R.id.add_alarm_fab);
        mAddPaperFab = rootView.findViewById(R.id.add_person_fab);

        chat = rootView.findViewById(R.id.msg);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ViewChatsActivity.class);
                i.putExtra("ID", userId);
                i.putExtra("Name", name);
                i.putExtra("Username", username);
                startActivity(i);
            }
        });

        viewall = rootView.findViewById(R.id.viewall);
        viewall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ViewAllDocumentActivity.class);
                i.putExtra("ID", userId);
                i.putExtra("Name", name);
                i.putExtra("Username", username);
                startActivity(i);
            }
        });

        viewallpost = rootView.findViewById(R.id.viewallpost);
        viewallpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ViewAllPostActivity.class);
                i.putExtra("ID", userId);
                i.putExtra("Name", name);
                i.putExtra("Username", username);
                startActivity(i);
            }
        });

        addPostActionText = rootView.findViewById(R.id.add_alarm_action_text);
        addPaperActionText = rootView.findViewById(R.id.add_person_action_text);

        mAddPostFab.setVisibility(View.GONE);
        mAddPaperFab.setVisibility(View.GONE);
        addPostActionText.setVisibility(View.GONE);
        addPaperActionText.setVisibility(View.GONE);

        // Retrieve data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recommendedItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String bookName = (String) snapshot.getValue();

                    int firstUnderscoreIndex = bookName.indexOf('_');
                    int secondUnderscoreIndex = bookName.indexOf('_', firstUnderscoreIndex + 1);
                    int thirdUnderscoreIndex = bookName.indexOf('_', secondUnderscoreIndex+1);
                    String username, bn;
                    if (bookName.contains("com")) {
                        username = bookName.substring(0, secondUnderscoreIndex);
                        bn = bookName.substring(thirdUnderscoreIndex+1);
                    } else {
                        username = bookName.substring(0, firstUnderscoreIndex);
                        bn = bookName.substring(secondUnderscoreIndex+1);
                    }

                    // Retrieve documentTitle and documentDescription from the database
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Documents").child(username).child(bookName);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String documentTitle = dataSnapshot.child("documentTitle").getValue(String.class);
                                String documentDescription = dataSnapshot.child("documentDescription").getValue(String.class);
                                String courseType = dataSnapshot.child("selectedCourseCode").getValue(String.class);
                                recommendedItems.add(new RecommendedItem(bn, documentTitle, documentDescription, R.drawable.pdficon, courseType));
                                recommendedAdapter.notifyDataSetChanged(); // Notify adapter of data change
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReferencePost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                recommendedPostItems.clear();

                for (DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {
                    String postid = (String) snapshot1.getValue();

                    final String[] authorNameSuggest = new String[1];

                    DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Posts");
                    reff.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshott) {
                            for(DataSnapshot snapshot2 : snapshott.getChildren()){
                                authorNameSuggest[0] = (String) snapshot2.getKey();
                                // Retrieve posttitle and postcaption, postdesc from the database
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(authorNameSuggest[0]).child(postid);
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String posttitle = postid;
                                            String postcaption = dataSnapshot.child("caption").getValue(String.class);
                                            String postdescription = dataSnapshot.child("description").getValue(String.class);
                                            String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                                            String authorName = dataSnapshot.child("author").getValue(String.class);
                                            recommendedPostItems.add(new RecommendedPostItem(posttitle, postcaption, postdescription, imageUrl, authorName));
                                            recommendedPostAdapter.notifyDataSetChanged(); // Notify adapter of data change
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle database error
                                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        isAllFabsVisible = false;

        mAddFab.shrink();

        mAddFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {

                            mAddPostFab.show();
                            mAddPaperFab.show();
                            addPostActionText.setVisibility(View.VISIBLE);
                            addPaperActionText.setVisibility(View.VISIBLE);

                            mAddFab.extend();

                            isAllFabsVisible = true;
                        } else {

                            mAddPostFab.hide();
                            mAddPaperFab.hide();
                            addPostActionText.setVisibility(View.GONE);
                            addPaperActionText.setVisibility(View.GONE);

                            mAddFab.shrink();

                            isAllFabsVisible = false;
                        }
                    }
                });

        // Handle the add document FAB click event
        mAddPaperFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UploadDocumentActivity.class);
                intent.putExtra("ID", userId);
                intent.putExtra("Name", name);
                intent.putExtra("Username", username);
                startActivity(intent);
            }
        });

        mAddPostFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), UploadPostActivity.class);
                        intent.putExtra("ID", userId);
                        intent.putExtra("Name", name);
                        intent.putExtra("Username", username);
                        startActivity(intent);                    }
                });

        return rootView;
    }

    private void handleImageClick(int position) {
//        String url = "";
//        switch (position) {
//            case 0:
//                url = "https://www.du.edu/it/services/security/5-url-warning-signs";
//                break;
//            case 1:
//                url = "https://velecor.com/10-common-internet-security-threats-and-how-to-avoid-them/";
//                break;
//            case 2:
//                url = "https://study.com/academy/lesson/how-social-networks-are-used-in-cybercrime.html";
//                break;
//        }

        Intent i = new Intent(getContext(), ViewAllDocumentActivity.class);
        i.putExtra("ID", userId);
        i.putExtra("Name", name);
        i.putExtra("Username", username);
        startActivity(i);

//        if (!url.isEmpty()) {
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//        }
    }

    private void startAutoImageSlider() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = imageSlider.getCurrentItem();
                int nextItem = (currentItem + 1) % imageList.size();
                imageSlider.setCurrentItem(nextItem);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }
}
